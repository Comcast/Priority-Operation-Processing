package com.comcast.pop.handler.executor.impl.executor.resident;

import com.comcast.pop.handler.executor.impl.processor.OperationWrapper;
import com.comcast.pop.api.operation.Operation;
import com.comast.pop.handler.base.ResidentHandler;
import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.comcast.pop.handler.executor.impl.executor.BaseOperationExecutor;
import com.comcast.pop.handler.executor.impl.executor.OperationExecutorFactory;
import com.comcast.pop.handler.executor.impl.registry.resident.operations.ResidentOperationRegistry;

/**
 * OperationExecutor factory for resident handlers.
 */
public class ResidentOperationExecutorFactory extends OperationExecutorFactory
{
    private ResidentOperationRegistry residentOperationsRegistry;

    public ResidentOperationExecutorFactory()
    {
        residentOperationsRegistry = new ResidentOperationRegistry();
    }

    @Override
    public BaseOperationExecutor createOperationExecutor(ExecutorContext executorContext, OperationWrapper operationWrapper)
    {
        Operation operation = operationWrapper.getOperation();

        ResidentHandler residentHandler = residentOperationsRegistry.getHandler(executorContext, operation.getType());
        // if no resident handler was found it is fine, if one was found pass it back
        return residentHandler == null ? null : new ResidentOperationExecutor(operation, residentHandler, executorContext.getLaunchDataWrapper());
    }

    public void setResidentOperationsRegistry(ResidentOperationRegistry residentOperationsRegistry)
    {
        this.residentOperationsRegistry = residentOperationsRegistry;
    }

    public ResidentOperationRegistry getResidentOperationsRegistry()
    {
        return residentOperationsRegistry;
    }
}
