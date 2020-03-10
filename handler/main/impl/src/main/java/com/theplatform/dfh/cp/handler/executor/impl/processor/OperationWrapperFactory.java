package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.api.operation.Operation;

public class OperationWrapperFactory
{
    public OperationWrapper create(Operation operation)
    {
        return new OperationWrapper(operation);
    }
}
