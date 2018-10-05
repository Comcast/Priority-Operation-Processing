package com.theplatform.dfh.cp.handler.executor.impl.processor.runner;

import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OnOperationCompleteListener;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OperationWrapper;

/**
 * Basic factory for creating OperationRunners
 */
public class OperationRunnerFactory
{
    public OperationRunner createOperationRunner(OperationWrapper operationWrapper, ExecutorContext executorContext, OnOperationCompleteListener onOperationCompleteListener)
    {
        return new OperationRunner(operationWrapper, executorContext, onOperationCompleteListener);
    }
}
