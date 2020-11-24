package com.pl.rst.training.process;

import com.pl.rst.training.GenerateReferenceNumber;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.stream.Collectors;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Deployment(resources = {
        "processes/ExampleTestProcess.bpmn",
})
public class ExampleTestProcessSequentialTest {
    @Rule
    @ClassRule
    public static ProcessEngineRule rule =
            TestCoverageProcessEngineRuleBuilder.create()
                    .withDetailedCoverageLogging().build();

    private HistoryService historyService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mocks.register("generateReferenceNumber", new GenerateReferenceNumber());
        historyService = rule.getHistoryService();
    }

    @Test
    public void testPositiveDecisionScenario() {
        ProcessInstance processInstance = rule.getRuntimeService()
                .startProcessInstanceByKey("ExampleTestProcess", "businessKey");

        assertThat(processInstance)
                .isStarted()
                .isWaitingAt("ProvideDecision");

        complete(
                rule.getTaskService().createTaskQuery().taskDefinitionKey("ProvideDecision").singleResult(),
                withVariables("loanDecision", "positive")
        );

        assertThat(processInstance)
                .isEnded();

        Map<String, Object> variables = historyService.createHistoricVariableInstanceQuery().list().stream()
                .collect(Collectors.toMap(HistoricVariableInstance::getName, HistoricVariableInstance::getValue));
        assertTrue(variables.containsKey("referenceNumber"));
    }

    @Test
    public void testNegativeDecisionScenario() {
        ProcessInstance processInstance = rule.getRuntimeService()
                .startProcessInstanceByKey("ExampleTestProcess", "businessKey");

        assertThat(processInstance)
                .isStarted()
                .isWaitingAt("ProvideDecision");

        complete(
                rule.getTaskService().createTaskQuery().taskDefinitionKey("ProvideDecision").singleResult(),
                withVariables("loanDecision", "negative")
        );

        assertThat(processInstance)
                .isStarted()
                .isWaitingAt("ReviewDecision");

        complete(
                rule.getTaskService().createTaskQuery().taskDefinitionKey("ReviewDecision").singleResult()
        );

        assertThat(processInstance)
                .isEnded();

        Map<String, Object> variables = historyService.createHistoricVariableInstanceQuery().list().stream()
                .collect(Collectors.toMap(HistoricVariableInstance::getName, HistoricVariableInstance::getValue));
        assertFalse(variables.containsKey("referenceNumber"));
    }
}
