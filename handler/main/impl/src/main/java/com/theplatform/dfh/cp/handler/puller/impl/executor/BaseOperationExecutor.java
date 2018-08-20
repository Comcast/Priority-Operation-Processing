package com.theplatform.dfh.cp.handler.puller.impl.executor;

import com.theplatform.dfh.cp.api.operation.Operation;

import java.util.UUID;

/**
 * TODO: rename to anything else...
 */
public abstract class BaseOperationExecutor
{
    protected Operation operation;

    public BaseOperationExecutor(Operation operation)
    {
        this.operation = operation;
    }

    public abstract String execute(String payload);

    protected static String generateContainerNameSuffix()
    {
        return "-" + UUID.randomUUID().toString();
    }
}