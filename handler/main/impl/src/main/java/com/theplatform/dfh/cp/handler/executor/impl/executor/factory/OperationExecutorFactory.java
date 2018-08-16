package com.theplatform.dfh.cp.handler.executor.impl.executor.factory;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.base.perform.Executor;
import com.theplatform.dfh.cp.handler.executor.impl.context.HandlerContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;

public interface OperationExecutorFactory
{
    BaseOperationExecutor getOperationExecutor(HandlerContext handlerContext, Operation operation);
}