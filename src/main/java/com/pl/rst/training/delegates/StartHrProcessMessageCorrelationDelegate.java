package com.pl.rst.training.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class StartHrProcessMessageCorrelationDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        delegateExecution.getProcessEngine().getRuntimeService().startProcessInstanceByMessage("START_PROCESS", delegateExecution.getProcessBusinessKey());
    }
}
