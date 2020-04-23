package com.comast.pop.handler.base.progress;

import com.comcast.pop.api.progress.CompleteStateMessage;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

public class OperationProgressFactory
{
    private final Date startTime;

    public OperationProgressFactory(Date startTime)
    {
        this.startTime = startTime;
    }

    public OperationProgressFactory()
    {
        this(new Date());
    }

    public OperationProgress create(ProcessingState processingState, String processingStateMessage)
    {
        return createWithEvents(processingState, processingStateMessage, null);
    }

    public OperationProgress create(ProcessingState processingState, String processingStateMessage, Double percentComplete)
    {
        OperationProgress operationProgress = createWithEvents(processingState, processingStateMessage, null);
        operationProgress.setPercentComplete(percentComplete);
        return operationProgress;
    }

    public OperationProgress createWithEvents(ProcessingState processingState, String processingStateMessage, List<DiagnosticEvent> diagnosticEvents)
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setProcessingState(processingState);
        operationProgress.setProcessingStateMessage(processingStateMessage);
        operationProgress.setStartedTime(startTime);
        operationProgress.setDiagnosticEvents(diagnosticEvents == null ? null : diagnosticEvents.toArray(new DiagnosticEvent[0]));
        switch (processingState)
        {
            case COMPLETE:
                // on successful completion always just set to 100%
                if(StringUtils.equals(processingStateMessage, CompleteStateMessage.SUCCEEDED.toString()))
                {
                    operationProgress.setPercentComplete(100d);
                }
                operationProgress.setCompletedTime(new Date());
                break;
        }
        return operationProgress;
    }
}
