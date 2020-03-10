package com.theplatform.dfh.cp.handler.executor.impl.processor.parallel;

import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OperationWrapper;

/**
 * Modifier for adjusting the state of the conductor
 *
 * TODO: consider an interface for the OperationConductor to limit the access on the object itself (not really critical in this context)
 */
public interface OperationConductorModifier
{
    void modify(ExecutorContext executorContext, OperationWrapper operationWrapper, OperationConductor operationConductor);
}
