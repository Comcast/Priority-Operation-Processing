package com.theplatform.dfh.cp.handler.executor.impl.executor.resident;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.base.ResidentHandler;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resident handler executor
 */
public class ResidentOperationExecutor extends BaseOperationExecutor
{
    private static final Logger logger = LoggerFactory.getLogger(ResidentOperationExecutor.class);
    private ResidentHandler residentHandler;

    public ResidentOperationExecutor(Operation operation, ResidentHandler residentHandler)
    {
        super(operation);
        this.residentHandler = residentHandler;
    }

    @Override
    public String execute(String payload)
    {
        logger.info("Operation {} INPUT  Payload: {}", operation.getId(), payload);
        String outputPayload = residentHandler.execute(payload);
        logger.info("Operation {} OUTPUT Payload: {}", operation.getId(), outputPayload);
        return outputPayload;
    }
}
