package com.theplatform.dfh.cp.handler.executor.impl.executor.local;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.executor.OperationExecutorFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

/**
 * Factory for producing executors to get MediaProperties (via a mediainfo launch through a docker container).
 * This may only apply to functional tests.
 */
public class LocalOperationExecutorFactory extends OperationExecutorFactory
{
    @Override
    public BaseOperationExecutor createOperationExecutor(ExecutorContext executorContext, Operation operation)
    {
        return new LocalOperationExecutor(operation, executorContext.getLaunchDataWrapper());
    }
}