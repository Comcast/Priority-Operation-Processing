package com.theplatform.dfh.cp.handler.executor.impl.executor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.resident.ResidentOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.registry.resident.operations.ResidentOperationsRegistry;

public abstract class OperationExecutorFactory
{
    private ResidentOperationExecutorFactory residentOperationExecutorFactory;

    /**
     * Creates the OperationExecutor based on the determined type (resident or external)
     * @param executorContext The context to use to build the OperationExecutor
     * @param operation Operation definition
     * @return The OperationExecutor to process the operation
     */
    public BaseOperationExecutor generateOperationExecutor(ExecutorContext executorContext, Operation operation)
    {
        // if this is a resident operation use that, otherwise create an op executor
        BaseOperationExecutor executor = residentOperationExecutorFactory.createOperationExecutor(executorContext, operation);
        return (executor != null) ? executor : createOperationExecutor(executorContext, operation);
    }

    /**
     * Creates the actual OperationExecutor
     * @param executorContext The context to use to build the OperationExecutor
     * @param operation Operation definition
     * @return The OperationExecutor to process the operation
     */
    protected abstract BaseOperationExecutor createOperationExecutor(ExecutorContext executorContext, Operation operation);

    public OperationExecutorFactory setResidentOperationExecutorFactory(ResidentOperationExecutorFactory residentOperationExecutorFactory)
    {
        this.residentOperationExecutorFactory = residentOperationExecutorFactory;
        return this;
    }
}