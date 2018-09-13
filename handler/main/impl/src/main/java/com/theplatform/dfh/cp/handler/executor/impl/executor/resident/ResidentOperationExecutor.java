package com.theplatform.dfh.cp.handler.executor.impl.executor.resident;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.base.ResidentHandler;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;
import com.theplatform.dfh.cp.handler.reporter.log.JsonReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resident handler executor
 */
public class ResidentOperationExecutor extends BaseOperationExecutor
{
    private static final Logger logger = LoggerFactory.getLogger(ResidentOperationExecutor.class);
    private ResidentHandler residentHandler;
    private Reporter reporter;

    public ResidentOperationExecutor(Operation operation, ResidentHandler residentHandler, LaunchDataWrapper launchDataWrapper)
    {
        super(operation, launchDataWrapper);
        this.residentHandler = residentHandler;
        this.reporter = new JsonReporter();
    }

    @Override
    public String execute(String payload)
    {
        logger.info("Operation {} INPUT  Payload: {}", operation.getId(), payload);
        String outputPayload = residentHandler.execute(payload, launchDataWrapper, reporter);
        logger.info("Operation {} OUTPUT Payload: {}", operation.getId(), outputPayload);
        return outputPayload;
    }

    public void setReporter(Reporter reporter)
    {
        this.reporter = reporter;
    }
}
