package com.comcast.fission.handler.executor.impl.resident.log;

import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.base.progress.OperationProgressFactory;
import com.theplatform.dfh.cp.handler.base.resident.BaseResidentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogResidentHandler extends BaseResidentHandler<LogHandlerInput, OperationProgressFactory>
{
    private static final Logger logger = LoggerFactory.getLogger(LogResidentHandler.class);

    @Override
    public String execute(LogHandlerInput logHandlerInput)
    {
        if(logHandlerInput != null && logHandlerInput.getLogMessages() != null)
            logHandlerInput.getLogMessages().forEach(logger::info);
        getProgressReporter().reportProgress(getOperationProgressFactory().create(ProcessingState.COMPLETE, CompleteStateMessage.SUCCEEDED.toString()));
        return null;
    }

    @Override
    public OperationProgressFactory getOperationProgressFactory()
    {
        return new OperationProgressFactory();
    }

    @Override
    public Class<LogHandlerInput> getPayloadClassType()
    {
        return LogHandlerInput.class;
    }
}
