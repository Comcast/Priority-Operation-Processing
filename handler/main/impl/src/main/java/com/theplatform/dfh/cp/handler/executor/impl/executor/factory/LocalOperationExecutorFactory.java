package com.theplatform.dfh.cp.handler.executor.impl.executor.factory;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.base.perform.Executor;
import com.theplatform.dfh.cp.handler.executor.impl.context.HandlerContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.executor.LocalOperationExecutor;

/**
 * Factory for producing executors to get MediaProperties (via a mediainfo launch through a docker container).
 * This may only apply to functional tests.
 */
public class LocalOperationExecutorFactory implements OperationExecutorFactory
{
    @Override
    public BaseOperationExecutor getOperationExecutor(HandlerContext handlerContext, Operation operation)
    {
        return new LocalOperationExecutor(operation);
    }
}