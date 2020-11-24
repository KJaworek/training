package com.pl.rst.training;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class GenerateReferenceNumber implements JavaDelegate {

  @Override
  public void execute(DelegateExecution ctx) throws Exception {
    ctx.setVariable("referenceNumber", new ObjectIdGenerators.UUIDGenerator().generateId(this));
  }
}
