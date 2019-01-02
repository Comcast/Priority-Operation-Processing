package com.theplatform.dfh.cp.handler.executor.impl.executor.resident;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.base.ResidentHandler;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporter;
import com.theplatform.dfh.cp.handler.reporter.log.JsonReporter;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.OperationProgressProvider;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        try
        {
            String progressJson = reporter.getLastProgress();
            OperationProgress operationProgress = jsonHelper.getObjectFromString(progressJson, OperationProgress.class);
            operationProgress.setOperation(operation.getName());
            operationProgress.setResultPayload(outputPayload);
            return operationProgress;
        }
        catch(JsonHelperException je)
        {
            logger.error("Unable to convert progress string to OperationProgress", je);
        }
        catch(Exception e)
        {
            logger.error("Unable to pull last progress form resident handler.",  e);
        }
        return null;
    }

    @Override
    public String execute(String payload)
    {
        logger.info("Operation {} INPUT  Payload: {}", operation.getId(), payload);
        outputPayload = residentHandler.execute(payload, launchDataWrapper, reporter);
        logger.info("Operation {} OUTPUT Payload: {}", operation.getId(), outputPayload);
        return outputPayload;
    }
}
