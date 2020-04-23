package com.comcast.pop.handler.executor.impl.processor.runner;

import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.comcast.pop.handler.executor.impl.processor.OnOperationCompleteListener;
import com.comcast.pop.handler.executor.impl.processor.OperationWrapper;

/**
 * Basic factory for creating OperationRunners
 */
public class OperationRunnerFactory
{
    public OperationRunner createOperationRunner(OperationWrapper operationWrapper, ExecutorContext executorContext, OnOperationCompleteListener onOperationCompleteListener)
    {
        return new OperationRunner(operationWrapper, executorContext, onOperationCompleteListener);
    }

    public void shutdown()
    {
    }
}
