package com.theplatform.dfh.cp.handler.executor.impl.executor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;

public interface OperationExecutorFactory
{
    BaseOperationExecutor getOperationExecutor(ExecutorContext executorContext, Operation operation);
}