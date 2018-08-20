package com.theplatform.dfh.cp.handler.puller.impl.executor.local;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.puller.impl.context.HandlerContext;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.puller.impl.executor.OperationExecutorFactory;

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