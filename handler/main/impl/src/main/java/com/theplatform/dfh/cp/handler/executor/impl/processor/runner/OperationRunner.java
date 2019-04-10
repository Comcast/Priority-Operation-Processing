package com.theplatform.dfh.cp.handler.executor.impl.processor.runner;

import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.messages.ExecutorMessages;
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
        // TODO: should this perform any retries?
        try
        {
            BaseOperationExecutor executor = executorContext.getOperationExecutorFactory().generateOperationExecutor(executorContext, operationWrapper.getOperation());
            // register the executor as a provider of operation progress
            executorContext.getAgendaProgressReporter().registerOperationProgressProvider(executor);
            String outputPayload = executor.execute(operationWrapper.getInputPayload());
            // get the last progress
            OperationProgress operationProgress = executor.retrieveOperationProgress();
            switch (operationProgress.getProcessingState())
            {
                case COMPLETE:
                    evaluateCompletedOperation(operationWrapper, operationProgress, outputPayload);
                    break;
                default:
                    // TODO: make a new diagnostic indicating things went wrong ?
                    operationWrapper.setSuccess(false);
                    break;
            }
        }
        catch(Throwable t)
        {
            String message = ExecutorMessages.OPERATION_EXECUTION_ERROR.getMessage(
                operationWrapper.getOperation() == null
                   ? "unknown!"
                   : operationWrapper.getOperation().getName());
            logger.error(message, t);
            operationWrapper.setSuccess(false);
            operationWrapper.addDiagnosticEvent(new DiagnosticEvent(message, t));
        }
        // always call the onComplete (critical for the operation conductor)
        if(onOperationCompleteListener != null) onOperationCompleteListener.onComplete(operationWrapper);
    }

    private void evaluateCompletedOperation(OperationWrapper operationWrapper, OperationProgress operationProgress, String outputPayload)
    {
        if(CompleteStateMessage.SUCCEEDED.toString().equals(operationProgress.getProcessingStateMessage()))
        {
            operationWrapper.setOutputPayload(outputPayload);
            operationWrapper.setSuccess(true);
        }
        else
        {
            // TODO: new diagnostic
            //operationWrapper.setOutputPayload();
            operationWrapper.setSuccess(false);
        }
    }
}
