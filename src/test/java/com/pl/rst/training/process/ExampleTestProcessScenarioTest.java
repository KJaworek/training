package com.pl.rst.training.process;

import com.pl.rst.training.GenerateReferenceNumber;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.delegate.TaskDelegate;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.stream.Collectors;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@Deployment(resources = {
        "processes/ExampleTestProcess.bpmn",
})
public class ExampleTestProcessScenarioTest {
    @Rule
    @ClassRule
    public static ProcessEngineRule rule =
            TestCoverageProcessEngineRuleBuilder.create()
                    .withDetailedCoverageLogging().build();
    @Mock
    private ProcessScenario exampleTest;

    private HistoryService historyService;
    private VariableMap variables;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mocks.register("generateReferenceNumber", new GenerateReferenceNumber());
        variables = Variables.createVariables();
        historyService = rule.getHistoryService();
    }

    @Test
    public void testPositiveDecisionScenario() {
        when(exampleTest.waitsAtUserTask("ProvideDecision")).thenReturn((task) -> {
            task.complete(withVariables("loanDecision", "positive"));
        });

        Scenario scenario = Scenario.run(exampleTest)
                .startByKey("ExampleTestProcess", variables)
                .execute();

        verify(exampleTest, times(1)).hasCompleted("GenerateReferenceNumber");
        verify(exampleTest).hasFinished("PositiveDecisionEndEvent");

        Map<String, Object> variables = historyService.createHistoricVariableInstanceQuery().list().stream()
                .collect(Collectors.toMap(HistoricVariableInstance::getName, HistoricVariableInstance::getValue));
        assertTrue(variables.containsKey("referenceNumber"));
    }

    @Test
    public void testNegativeDecisionScenario() {
        when(exampleTest.waitsAtUserTask("ProvideDecision")).thenReturn((task) -> {
            task.complete(withVariables("loanDecision", "negative"));
        });
        when(exampleTest.waitsAtUserTask("ReviewDecision")).thenReturn(TaskDelegate::complete);

        Scenario scenario = Scenario.run(exampleTest)
                .startByKey("ExampleTestProcess", variables)
                .execute();

        verify(exampleTest, times(0)).hasCompleted("GenerateReferenceNumber");
        verify(exampleTest).hasFinished("NegativeDecisionEndEvent");
    }
}
