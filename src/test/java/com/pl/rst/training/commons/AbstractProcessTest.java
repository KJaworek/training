package com.pl.rst.training.commons;

import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.scenario.ProcessScenario;
import org.junit.After;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractProcessTest {

    protected static final String BUSINESS_KEY = "businessKey";

    @Mock
    protected ProcessScenario processScenario;


    protected Map<String, Object> variables;

    protected List<Deployment> deployedDummies = new ArrayList<>();

    protected abstract ProcessEngineRule getProcessEngineRule();

    protected void registerServiceTaskMock(String taskName, JavaDelegate delegate) {
        Mocks.register(taskName, delegate);
    }

    protected void registerTaskDelegateMock(String taskName, TaskListener delegate) {
        Mocks.register(taskName, delegate);
    }

    protected void completeSingleInstanceTimer(String timerId) {
        String jobId = getProcessEngineRule().getManagementService().createJobQuery().activityId(timerId).singleResult().getId();
        getProcessEngineRule().getManagementService().executeJob(jobId);
    }

    protected void deployMockedSubProcess(String procDefKey) {
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess() //
                .id(procDefKey) //
                .startEvent() //
                .endEvent() //
                .done();

        org.camunda.bpm.engine.repository.Deployment deployment =
                getProcessEngineRule().getProcessEngine().getRepositoryService().createDeployment()
                        .addModelInstance(procDefKey + ".bpmn", modelInstance).deploy();

        deployedDummies.add(deployment);
    }

    protected void deployMockedSubProcessWithUserTask(String procDefKey, String userTaskId) {
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess() //
                .id(procDefKey) //
                .startEvent() //
                .userTask(userTaskId)
                .endEvent() //
                .done();

        org.camunda.bpm.engine.repository.Deployment deployment =
                getProcessEngineRule().getProcessEngine().getRepositoryService().createDeployment()
                        .addModelInstance(procDefKey + ".bpmn", modelInstance).deploy();

        deployedDummies.add(deployment);
    }

    protected void deployMockedSubProcessWithServiceTask(String procDefKey, String serviceDelegateMock) {
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess() //
                .id(procDefKey) //
                .startEvent() //
                .serviceTask().camundaDelegateExpression("${" + serviceDelegateMock + "}")
                .endEvent() //
                .done();

        org.camunda.bpm.engine.repository.Deployment deployment =
                getProcessEngineRule().getProcessEngine().getRepositoryService().createDeployment()
                        .addModelInstance(procDefKey + ".bpmn", modelInstance).deploy();

        deployedDummies.add(deployment);
    }

    @After
    public void deleteDummies() {
        for (org.camunda.bpm.engine.repository.Deployment deployment : deployedDummies) {
            getProcessEngineRule().getProcessEngine().getRepositoryService().deleteDeployment(deployment.getId());
        }
        deployedDummies.clear();
    }
}
