package com.pl.rst.training;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SendApplicationDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("name", delegateExecution.getVariable("name"));
        delegateExecution.getProcessEngine().getRuntimeService()
                .createMessageCorrelation("APPLICATION")
                .processInstanceBusinessKey(String.valueOf(delegateExecution.getVariable("jobId")))
                .correlate();

    }
}
