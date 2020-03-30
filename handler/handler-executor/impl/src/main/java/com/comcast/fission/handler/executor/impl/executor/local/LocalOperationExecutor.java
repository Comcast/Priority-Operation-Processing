package com.comcast.fission.handler.executor.impl.executor.local;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.base.ResidentHandlerParams;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.reporter.LogReporter;
import com.comcast.fission.handler.executor.impl.executor.BaseOperationExecutor;
import com.comcast.fission.handler.executor.impl.resident.SampleResidentHandler;
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
    private String outputPayload;

    public LocalOperationExecutor(Operation operation, LaunchDataWrapper launchDataWrapper)
    {
        super(operation, launchDataWrapper);
    }

    @Override
    public OperationProgress retrieveOperationProgress()
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setOperation(operation.getName());
        operationProgress.setResultPayload(outputPayload);
        return operationProgress;
    }

    @Override
    public String execute(String payload)
    {
        logger.info("Operation {} INPUT  Payload: {}", operation.getId(), payload);
        outputPayload = new SampleResidentHandler().execute(new ResidentHandlerParams()
            .setOperation(operation)
            .setPayload(payload)
            .setLaunchDataWrapper(launchDataWrapper)
            .setReporter(new LogReporter<>())
        );
        logger.info("Operation {} OUTPUT Payload: {}", operation.getId(), outputPayload);
        return outputPayload;
    }
}