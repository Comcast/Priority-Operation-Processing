package com.theplatform.dfh.cp.handler.executor.impl.executor.resident;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.base.ResidentHandler;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.log.JsonReporter;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Resident handler executor. This always uses a JsonReporter (just an in-memory reporter)
 */
public class ResidentOperationExecutor extends BaseOperationExecutor
{
    private static final Logger logger = LoggerFactory.getLogger(ResidentOperationExecutor.class);
    private ResidentHandler residentHandler;
    private JsonReporter reporter;
    private JsonHelper jsonHelper;
    private String outputPayload;
    private Date startTime;
    private Date completedTime;
    private Exception residentHandlerException;

    public ResidentOperationExecutor(Operation operation, ResidentHandler residentHandler, LaunchDataWrapper launchDataWrapper)
    {
        super(operation, launchDataWrapper);
        this.residentHandler = residentHandler;
        this.reporter = new JsonReporter();
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

        operationProgress = operationProgress == null ? new OperationProgress() : operationProgress;
        operationProgress.setStartedTime(startTime);
        operationProgress.setCompletedTime(completedTime);
        operationProgress.setOperation(operation.getName());
        operationProgress.setResultPayload(outputPayload);
        if(residentHandlerException != null)
        {
            // TODO: diagnositcs with the exception
            operationProgress.setProcessingState(ProcessingState.COMPLETE);
            operationProgress.setProcessingStateMessage(CompleteStateMessage.FAILED.toString());
        }
        return operationProgress;
    }

    @Override
    public String execute(String payload)
    {
        startTime = new Date();
        logger.info("Operation {} INPUT  Payload: {}", operation.getId(), payload);
        try
        {
            outputPayload = residentHandler.execute(payload, launchDataWrapper, reporter);
        }
        catch(Exception e)
        {
            residentHandlerException = e;
            throw e;
        }
        completedTime = new Date();
        logger.info("Operation {} OUTPUT Payload: {}", operation.getId(), outputPayload);
        return outputPayload;
    }
}
