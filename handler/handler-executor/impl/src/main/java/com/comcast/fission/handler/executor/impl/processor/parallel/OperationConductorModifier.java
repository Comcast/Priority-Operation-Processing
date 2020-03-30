package com.comcast.fission.handler.executor.impl.processor.parallel;

import com.comcast.fission.handler.executor.impl.processor.OperationWrapper;
import com.comcast.fission.handler.executor.impl.context.ExecutorContext;

/**
 * Modifier for adjusting the state of the conductor
 *
 * TODO: consider an interface for the OperationConductor to limit the access on the object itself (not really critical in this context)
 */
public interface OperationConductorModifier
{
    void modify(ExecutorContext executorContext, OperationWrapper operationWrapper, OperationConductor operationConductor);
}
