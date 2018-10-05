package com.theplatform.dfh.cp.handler.executor.impl.processor.runner;

import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OnOperationCompleteListener;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OperationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple wrapper for running an Operation
 *
 * TODO: expose the underlying executor so it can be interrupted (cancel due to whatever reason)
 */
public class OperationRunner implements Runnable
{
    private static Logger logger = LoggerFactory.getLogger(OperationRunner.class);

    private OperationWrapper operationWrapper;
    private ExecutorContext executorContext;
    private OnOperationCompleteListener onOperationCompleteListener;

    public OperationRunner(OperationWrapper operationWrapper, ExecutorContext executorContext, OnOperationCompleteListener onOperationCompleteListener)
    {
        this.operationWrapper = operationWrapper;
        this.executorContext = executorContext;
        this.onOperationCompleteListener = onOperationCompleteListener;
    }

    /**
     * Runs the operation after determining the type of operation execution required (resident or otherwise)
     */
    public void run()
    {
        try
        {
            BaseOperationExecutor executor = executorContext.getOperationExecutorFactory().generateOperationExecutor(executorContext, operationWrapper.getOperation());
            String outputPayload = executor.execute(operationWrapper.getInputPayload());
            operationWrapper.setOutputPayload(outputPayload);
            // TODO: op wrapper success flag
        }
        catch(Throwable t)
        {
            // TODO: op wrapper fail flag
            logger.error(String.format("Failed to execute operation: %1$s", operationWrapper.getOperation() == null ? "unknown!" : operationWrapper.getOperation().getName())
                , t);
        }
        // always call the onComplete (critical for the operation conductor)
        if(onOperationCompleteListener != null) onOperationCompleteListener.onComplete(operationWrapper);
    }
}
