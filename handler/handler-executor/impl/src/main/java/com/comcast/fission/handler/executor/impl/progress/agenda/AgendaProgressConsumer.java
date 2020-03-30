package com.comcast.fission.handler.executor.impl.progress.agenda;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;

public interface AgendaProgressConsumer
{
    void setAgendaProgress(AgendaProgress agendaProgress);
    void registerOperationProgressProvider(OperationProgressProvider operationProgressProvder);
    void setTotalProgressOperationCount(int operationTotalCount);
    void adjustTotalProgressOperationCount(int operationTotalCountAdjustment);
    void incrementCompletedOperationCount(int incrementAmount);
}
