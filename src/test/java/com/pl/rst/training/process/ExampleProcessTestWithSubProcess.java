package com.pl.rst.training.process;

import com.pl.rst.training.commons.AbstractProcessTest;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;

@Deployment(resources = {
        "processes/ExampleProcessTest2.bpmn",
})
public class ExampleProcessTestWithSubProcess extends AbstractProcessTest {

    private final static String TASK_SUFFIX = "_Task";
    private final static String SUB_PROCESS_DEF_KEY = "ExampleTestProcess";
    private final static String TEST_PROCESS_DEF_KEY = "ExampleProcessTest2";


    @Rule
    @ClassRule
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .excludeProcessDefinitionKeys(SUB_PROCESS_DEF_KEY)
            .build();

    @Override
    protected ProcessEngineRule getProcessEngineRule() {
        return rule;
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        deployMockedSubProcessWithUserTask(SUB_PROCESS_DEF_KEY, SUB_PROCESS_DEF_KEY + TASK_SUFFIX);
        registerServiceTaskMock("sampleDelegate", execution -> {
            execution.setVariable("var1", 1);
        });
    }

    @Test
    public void happyPath(){
        ProcessInstance processInstance = rule.getRuntimeService()
                .startProcessInstanceByKey(TEST_PROCESS_DEF_KEY, BUSINESS_KEY);

        assertThat(processInstance)
                .isStarted()
                .hasPassed("SERVICE1")
                .isWaitingAt("TASK1");

        complete(task("TASK1"));

        assertThat(processInstance)
                .isStarted()
                .hasPassed("TASK1")
                .isWaitingAt("TIMER1");

        execute(job("TIMER1"));

        assertThat(processInstance)
                .hasPassed("TIMER1")
                .isWaitingAt("SUB1");

        complete(rule.getTaskService().createTaskQuery().taskDefinitionKey(SUB_PROCESS_DEF_KEY + TASK_SUFFIX).singleResult());

        assertThat(processInstance)
                .hasPassed("SUB1")
                .isWaitingAt("MESSAGE1");

        rule.getProcessEngine().getRuntimeService().correlateMessage("MESSAGE1");

        assertThat(processInstance)
                .hasPassed("MESSAGE1")
                .isEnded();
    }

}
