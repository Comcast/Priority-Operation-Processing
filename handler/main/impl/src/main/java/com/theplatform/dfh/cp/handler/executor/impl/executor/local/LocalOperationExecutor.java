package com.theplatform.dfh.cp.handler.executor.impl.executor.local;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.resident.SampleResidentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local executor is just for testing/prototype. It does not perform any actual operations.
 * Internally it just uses a SampleResidentHandler
 * The resultPayload is based on the input class of the sample handler.
 */
public class LocalOperationExecutor extends BaseOperationExecutor
{
    private static final Logger logger = LoggerFactory.getLogger(LocalOperationExecutor.class);

    public LocalOperationExecutor(Operation operation)
    {
        super(operation);
    }

    @Override
    public String execute(String payload)
    {
        logger.info("Operation {} INPUT  Payload: {}", operation.getId(), payload);
        String outputPayload = new SampleResidentHandler().execute(payload);
        logger.info("Operation {} OUTPUT Payload: {}", operation.getId(), outputPayload);
        return outputPayload;
    }
}