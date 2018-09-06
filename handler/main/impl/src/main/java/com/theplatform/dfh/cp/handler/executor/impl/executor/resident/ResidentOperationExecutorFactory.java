package com.theplatform.dfh.cp.handler.executor.impl.executor.resident;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.base.ResidentHandler;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.executor.OperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.registry.resident.operations.ResidentOperationsRegistry;

/**
 * OperationExecutor factory for resident handlers.
 */
public class ResidentOperationExecutorFactory extends OperationExecutorFactory
{
    private ResidentOperationsRegistry residentOperationsRegistry;

    public ResidentOperationExecutorFactory()
    {
        residentOperationsRegistry = new ResidentOperationsRegistry();
    }

    @Override
    public BaseOperationExecutor createOperationExecutor(ExecutorContext executorContext, Operation operation)
    {
        ResidentHandler residentHandler = residentOperationsRegistry.getHandler(operation.getType());
        // if no resident handler was found it is fine, if one was found pass it back
        return residentHandler == null ? null : new ResidentOperationExecutor(operation, residentHandler);
    }

    public void setResidentOperationsRegistry(ResidentOperationsRegistry residentOperationsRegistry)
    {
        this.residentOperationsRegistry = residentOperationsRegistry;
    }
}
