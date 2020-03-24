package com.theplatform.dfh.cp.handler.executor.impl.processor.runner;

import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.messages.ExecutorMessages;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OnOperationCompleteListener;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OperationWrapper;
import com.theplatform.dfh.cp.handler.executor.impl.processor.runner.event.OperationCompleteEvent;
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

    private OperationCompleteEvent operationCompleteEvent;
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
            logger.debug("OperationWrapper [" + operationWrapper.getOperation().getName() + "]");
            if (executorContext.getJsonContext() == null)
            {
                logger.warn("Executor context is null");
            }
            else
            {
                logger.debug("Executor Context [" + executorContext.getJsonContext().toString() + "], Executor ProgressReporter [" +
                             executorContext.getAgendaProgressReporter() + "], Executor Reporter [" + executorContext.getReporter() + "]");
            }
            logger.debug("Executing start of Pod");
            BaseOperationExecutor executor = executorContext.getOperationExecutorFactory().generateOperationExecutor(executorContext, operationWrapper);
            // register the executor as a provider of operation progress
            executorContext.getAgendaProgressReporter().registerOperationProgressProvider(executor);
            String outputPayload = executor.execute(operationWrapper.getInputPayload());
            // get the last progress
            OperationProgress operationProgress = executor.retrieveOperationProgress();
            if(operationProgress == null)
            {
                setFailureOnOperationWrapper(
                    operationWrapper,
                    ExecutorMessages.OPERATION_EXECUTION_INCOMPLETE_NO_PROGRESS.getMessage(getOperationName(operationWrapper)),
                    null);
            }
            else
            {
                if(operationProgress.getProcessingState() == ProcessingState.COMPLETE)
                {
                    evaluateCompletedOperation(operationWrapper, operationProgress, outputPayload);
                }
                else
                {
                    setFailureOnOperationWrapper(
                        operationWrapper,
                        ExecutorMessages.OPERATION_EXECUTION_INCOMPLETE.getMessage(getOperationName(operationWrapper), operationProgress.getProcessingState()),
                        null);
                }
            }
        }
        catch(Throwable t)
        {
            setFailureOnOperationWrapper(
                operationWrapper,
                ExecutorMessages.OPERATION_EXECUTION_ERROR.getMessage(getOperationName(operationWrapper)),
                t);
        }
        // always call the onComplete (critical for the operation conductor)
        if(onOperationCompleteListener != null) onOperationCompleteListener.onComplete(operationWrapper);
    }

    private void setFailureOnOperationWrapper(OperationWrapper operationWrapper, String message, Throwable t)
    {
        logger.error(message, t);
        operationWrapper.setSuccess(false);
        operationWrapper.addDiagnosticEvent(new DiagnosticEvent(message, t));
    }
    protected String getOperationType()
    {
         return operationWrapper.getOperation() == null
                      ? "unknown"
                      : operationWrapper.getOperation().getType();
    }
    /**
     * Gets the operation name, defaulting if one is not set.
     * @param operationWrapper The operation wrapper to get the name from
     * @return The operation name or unknown if unset
     */
    private String getOperationName(OperationWrapper operationWrapper)
    {
        return operationWrapper.getOperation() == null
        ? "unknown"
        : operationWrapper.getOperation().getName();
    }

    private void evaluateCompletedOperation(OperationWrapper operationWrapper, OperationProgress operationProgress, String outputPayload)
    {
        if(operationCompleteEvent != null)
        {
            operationCompleteEvent.onOperationComplete(operationWrapper.getOperation(), operationProgress);
        }

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

    public OperationWrapper getOperationWrapper()
    {
        return operationWrapper;
    }

    public void setOperationCompleteEvent(OperationCompleteEvent operationCompleteEvent)
    {
        this.operationCompleteEvent = operationCompleteEvent;
    }
}
