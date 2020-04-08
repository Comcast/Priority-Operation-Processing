package com.comcast.pop.cp.endpoint.data;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;

public class EndpointObjectGenerator
{
    public static OperationProgress generateWaitingOperationProgress(Agenda agenda, Operation operation)
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setCustomerId(agenda.getCustomerId());
        operationProgress.setAgendaProgressId(agenda.getProgressId());
        operationProgress.setProcessingState(ProcessingState.WAITING);
        operationProgress.setOperation(operation.getName());
        operationProgress.setCid(agenda.getCid());
        operationProgress.setPercentComplete(0d);
        operationProgress.setId(OperationProgress.generateId(agenda.getProgressId(), operation.getName()));

        if (operation.getParams() != null)
        {
            ParamsMap params = new ParamsMap();
            params.putAll(operation.getParams());
            operationProgress.setParams(params);
        }
        return operationProgress;
    }
}
