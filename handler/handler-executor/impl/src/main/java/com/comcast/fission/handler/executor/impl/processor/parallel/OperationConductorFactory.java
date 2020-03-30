package com.comcast.fission.handler.executor.impl.processor.parallel;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.comcast.fission.handler.executor.impl.context.ExecutorContext;

import java.util.Collection;

/**
 * Basic factory for OperationConductor objects
 */
public class OperationConductorFactory
{
    /**
     * Creates an operation conductor
     * @param operations The operations to complete
     * @param executorContext The executor context for this app
     * @return an OperationConductor
     */
    public OperationConductor createOperationConductor(Collection<Operation> operations, ExecutorContext executorContext)
    {
        return new OperationConductor(operations, executorContext);
    }
}
