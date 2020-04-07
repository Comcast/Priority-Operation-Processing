package com.comast.pop.handler.base;

import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.progress.OperationProgress;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.reporter.ProgressReporter;

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
