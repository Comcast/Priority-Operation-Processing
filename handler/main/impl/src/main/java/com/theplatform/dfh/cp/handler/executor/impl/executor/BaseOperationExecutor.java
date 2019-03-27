package com.theplatform.dfh.cp.handler.executor.impl.executor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.OperationProgressProvider;

import java.util.UUID;

/**
 * Base for all types of operation executors
 */
public abstract class BaseOperationExecutor implements OperationProgressProvider
{
    protected Operation operation;
    protected LaunchDataWrapper launchDataWrapper;

    public BaseOperationExecutor(Operation operation, LaunchDataWrapper launchDataWrapper)
    {
        this.operation = operation;
        this.launchDataWrapper = launchDataWrapper;
    }

    public abstract String execute(String payload);

    protected static String generateContainerNameSuffix()
    {
        return "-" + UUID.randomUUID().toString();
    }

    public void setOperation(Operation operation)
    {
        this.operation = operation;
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }
}