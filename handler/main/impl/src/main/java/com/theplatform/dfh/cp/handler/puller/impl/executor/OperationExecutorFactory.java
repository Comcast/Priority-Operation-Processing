package com.theplatform.dfh.cp.handler.puller.impl.executor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.puller.impl.context.HandlerContext;

public interface OperationExecutorFactory
{
    BaseOperationExecutor getOperationExecutor(HandlerContext handlerContext, Operation operation);
}