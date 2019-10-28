package com.theplatform.dfh.cp.handler.executor.impl.executor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.progress.agenda.OperationProgressProvider;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

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