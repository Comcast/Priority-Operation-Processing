package com.theplatform.dfh.cp.endpoint.data;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;

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
