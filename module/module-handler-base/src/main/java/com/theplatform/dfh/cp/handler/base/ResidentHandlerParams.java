package com.theplatform.dfh.cp.handler.base;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.reporter.ProgressReporter;

public class ResidentHandlerParams
{
    private Operation operation;
    private String payload;
    private LaunchDataWrapper launchDataWrapper;
    private ProgressReporter<OperationProgress> reporter;

    public Operation getOperation()
    {
        return operation;
    }

    public ResidentHandlerParams setOperation(Operation operation)
    {
        this.operation = operation;
        return this;
    }

    public String getPayload()
    {
        return payload;
    }

    public ResidentHandlerParams setPayload(String payload)
    {
        this.payload = payload;
        return this;
    }

    public LaunchDataWrapper getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public ResidentHandlerParams setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
        return this;
    }

    public ProgressReporter<OperationProgress> getReporter()
    {
        return reporter;
    }

    public ResidentHandlerParams setReporter(ProgressReporter<OperationProgress> reporter)
    {
        this.reporter = reporter;
        return this;
    }
}
