package com.comcast.pop.handler.executor.impl.executor.resident;

import com.comcast.pop.handler.executor.impl.messages.ExecutorMessages;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.progress.CompleteStateMessage;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comast.pop.handler.base.ResidentHandler;
import com.comast.pop.handler.base.ResidentHandlerParams;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.reporter.JsonReporter;
import com.comcast.pop.handler.executor.impl.executor.BaseOperationExecutor;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.modules.jsonhelper.JsonHelperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Resident handler executor. This always uses a JsonReporter (just an in-memory reporter)
 */
public class ResidentOperationExecutor extends BaseOperationExecutor
{
    private static final Logger logger = LoggerFactory.getLogger(ResidentOperationExecutor.class);
    private ResidentHandler residentHandler;
    private JsonReporter<OperationProgress> reporter;
    private JsonHelper jsonHelper;
    private String outputPayload;
    private Date startTime;
    private Date completedTime;
    private Exception residentHandlerException;

    public ResidentOperationExecutor(Operation operation, ResidentHandler residentHandler, LaunchDataWrapper launchDataWrapper)
    {
        super(operation, launchDataWrapper);
        this.residentHandler = residentHandler;
        this.reporter = new JsonReporter<>();
        this.jsonHelper = new JsonHelper();
    }

    @Override
    public OperationProgress retrieveOperationProgress()
    {
        OperationProgress operationProgress = null;
        // try to extract the operation progress
        try
        {
            String progressJson = reporter.getLastProgress();
            if(progressJson != null)
            {
                operationProgress = jsonHelper.getObjectFromString(progressJson, OperationProgress.class);
            }
        }
        catch(JsonHelperException je)
        {
            logger.error("Unable to convert progress string to OperationProgress. Generating default.", je);
        }

        if(operationProgress == null)
        {
            logger.warn("Resident operation {} is not reporting progress.", operation.getName());
        }

        return generateOperationProgress(operationProgress);
    }

    @Override
    public String execute(String payload)
    {
        startTime = new Date();
        logger.info("Operation {} INPUT  Payload: {}", operation.getId(), payload);
        try
        {
            outputPayload = residentHandler.execute(new ResidentHandlerParams()
                .setOperation(operation)
                .setPayload(payload)
                .setLaunchDataWrapper(launchDataWrapper)
                .setReporter(reporter));
        }
        catch(Exception e)
        {
            // the exception will result in a failed operation progress as well as a DiagnosticEvent (in OperationRunnger)
            setResidentHandlerException(e);
        }
        completedTime = new Date();
        logger.info("Operation {} OUTPUT Payload: {}", operation.getId(), outputPayload);
        return outputPayload;
    }

    protected OperationProgress generateOperationProgress(OperationProgress existingProgress)
    {
        OperationProgress operationProgress = existingProgress == null ? new OperationProgress() : existingProgress;
        if(operationProgress.getProcessingState() == null)
            operationProgress.setProcessingState(ProcessingState.WAITING);
        operationProgress.setStartedTime(startTime);
        operationProgress.setCompletedTime(completedTime);
        operationProgress.setOperation(operation.getName());
        operationProgress.setResultPayload(outputPayload);
        if(residentHandlerException != null)
        {
            operationProgress.setProcessingState(ProcessingState.COMPLETE);
            operationProgress.setProcessingStateMessage(CompleteStateMessage.FAILED.toString());
            appendExceptionDiagnosticEvent(operationProgress, residentHandlerException);
        }
        return operationProgress;
    }

    /**
     * Appends a DiagnosticEvent wrapping the exception to the OperationProgress
     * @param operationProgress The progress to append
     * @param e The exception to wrap
     */
    private void appendExceptionDiagnosticEvent(OperationProgress operationProgress, Exception e)
    {
        List<DiagnosticEvent> diagnosticEvents = new LinkedList<>();

        diagnosticEvents.add(
            new DiagnosticEvent(
                ExecutorMessages.OPERATION_RESIDENT_EXECUTION_FAILED.getMessage(operation.getName()),
                e)
        );

        if(operationProgress.getDiagnosticEvents() != null)
            Collections.addAll(diagnosticEvents, operationProgress.getDiagnosticEvents());

        operationProgress.setDiagnosticEvents(diagnosticEvents.toArray(new DiagnosticEvent[0]));
    }

    protected void setResidentHandlerException(Exception residentHandlerException)
    {
        this.residentHandlerException = residentHandlerException;
    }
}
