package com.theplatform.dfh.cp.handler.executor.impl.executor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * TODO: rename to anything else...
 */
public abstract class BaseOperationExecutor
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