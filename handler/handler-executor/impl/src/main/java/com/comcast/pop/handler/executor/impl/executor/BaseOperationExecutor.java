package com.comcast.pop.handler.executor.impl.executor;

import com.comcast.pop.handler.executor.impl.progress.agenda.OperationProgressProvider;
import com.comcast.pop.api.operation.Operation;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;

/**
 * Base for all types of operation executors
 */
public abstract class BaseOperationExecutor implements OperationProgressProvider
{
    protected Operation operation;
    protected LaunchDataWrapper launchDataWrapper;
    protected String idenitifier;

    public BaseOperationExecutor(Operation operation, LaunchDataWrapper launchDataWrapper)
    {
        this.operation = operation;
        this.launchDataWrapper = launchDataWrapper;
    }

    public abstract String execute(String payload);

    public void setOperation(Operation operation)
    {
        this.operation = operation;
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public String getIdenitifier()
    {
        return idenitifier;
    }

    protected BaseOperationExecutor setIdenitifier(String idenitifier)
    {
        this.idenitifier = idenitifier;
        return this;
    }
}