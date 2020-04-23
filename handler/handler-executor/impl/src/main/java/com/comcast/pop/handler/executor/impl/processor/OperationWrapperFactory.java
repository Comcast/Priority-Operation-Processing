package com.comcast.pop.handler.executor.impl.processor;

import com.comcast.pop.api.operation.Operation;

public class OperationWrapperFactory
{
    public OperationWrapper create(Operation operation)
    {
        return new OperationWrapper(operation);
    }
}
