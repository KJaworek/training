package com.pl.rst.training;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.ThrowEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CorrelateMessageExecution implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        ThrowEvent event = (ThrowEvent) delegateExecution.getBpmnModelElementInstance();

        Optional<MessageEventDefinition> messageEventDefinitionOptional = event.getEventDefinitions().stream()
                .filter((ed) -> ed instanceof MessageEventDefinition)
                .map((ed) -> ((MessageEventDefinition) ed))
                .findFirst();
        messageEventDefinitionOptional.ifPresent((messageEventDefinition) -> {
            if (messageEventDefinition.getMessage() != null) {

                if (delegateExecution.getProcessBusinessKey() != null) {
                    RuntimeService runtimeService = delegateExecution.getProcessEngineServices().getRuntimeService();
                    String messageName = messageEventDefinition.getMessage().getName();

                    runtimeService.createMessageCorrelation(messageName).processInstanceBusinessKey(delegateExecution.getProcessBusinessKey()).correlateAll();
                }
            }
        });

    }
}