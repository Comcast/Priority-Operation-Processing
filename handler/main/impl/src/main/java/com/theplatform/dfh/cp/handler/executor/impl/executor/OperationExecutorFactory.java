package com.theplatform.dfh.cp.handler.executor.impl.executor;

import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.resident.ResidentOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OperationWrapper;

public abstract class OperationExecutorFactory
{
    private ResidentOperationExecutorFactory residentOperationExecutorFactory;

    /**
     * Creates the OperationExecutor based on the determined type (resident or external)
     * @param executorContext The context to use to build the OperationExecutor
     * @param operationWrapper OperationWrapper definition
     * @return The OperationExecutor to process the operation
     */
    public BaseOperationExecutor generateOperationExecutor(ExecutorContext executorContext, OperationWrapper operationWrapper)
    {
        // if this is a resident operationWrapper use that, otherwise create an op executor
        BaseOperationExecutor executor = residentOperationExecutorFactory.createOperationExecutor(executorContext, operationWrapper);
        if(executor == null)
        {
            executor = createOperationExecutor(executorContext, operationWrapper);
        }
        operationWrapper.setOperationExecutor(executor);
        return executor;
    }

    /**
     * Creates the actual OperationExecutor
     * @param executorContext The context to use to build the OperationExecutor
     * @param operation OperationWrapper definition
     * @return The OperationExecutor to process the operation
     */
    protected abstract BaseOperationExecutor createOperationExecutor(ExecutorContext executorContext, OperationWrapper operation);

    public OperationExecutorFactory setResidentOperationExecutorFactory(ResidentOperationExecutorFactory residentOperationExecutorFactory)
    {
        this.residentOperationExecutorFactory = residentOperationExecutorFactory;
        return this;
    }
}