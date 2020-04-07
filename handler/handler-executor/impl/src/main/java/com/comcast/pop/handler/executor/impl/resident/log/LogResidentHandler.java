package com.comcast.pop.handler.executor.impl.resident.log;

import com.comcast.pop.api.progress.CompleteStateMessage;
import com.comcast.pop.api.progress.ProcessingState;
import com.comast.pop.handler.base.progress.OperationProgressFactory;
import com.comast.pop.handler.base.resident.BaseResidentHandler;
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
