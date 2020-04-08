package com.comcast.pop.cp.endpoint;

import com.comcast.pop.api.AgendaInsight;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import com.comcast.pop.object.api.IdentifiedObject;

import java.util.Arrays;

public class TestUtil
{
    public static AgendaInsight createAgendaInsight(String insightId, String resourcePool)
    {
        AgendaInsight agendaInsight = new AgendaInsight();
        agendaInsight.setInsightId(insightId);
        agendaInsight.setResourcePoolId(resourcePool);
        return agendaInsight;
    }

    public static AgendaProgress createAgendaProgress(ProcessingState processingState, String processingStateMessage)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setProcessingState(processingState);
        agendaProgress.setProcessingStateMessage(processingStateMessage);
        return agendaProgress;
    }

    public static OperationProgress createOperationProgress(ProcessingState processingState, String processingStateMessage)
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setProcessingState(processingState);
        operationProgress.setProcessingStateMessage(processingStateMessage);
        return operationProgress;
    }

    public static <T extends IdentifiedObject> DataObjectResponse<T> createDataObjectResponse(T... args)
    {
        DataObjectResponse<T> response = new DefaultDataObjectResponse<>();
        Arrays.stream(args).forEach(response::add);
        return response;
    }

    public static <T extends IdentifiedObject> DataObjectResponse<T> createErrorDataObjecResponse(String message)
    {
        DataObjectResponse<T> dataObjectResponse = new DefaultDataObjectResponse<>();
        dataObjectResponse.setErrorResponse(ErrorResponseFactory.badRequest(message, null));
        return dataObjectResponse;
    }
}
