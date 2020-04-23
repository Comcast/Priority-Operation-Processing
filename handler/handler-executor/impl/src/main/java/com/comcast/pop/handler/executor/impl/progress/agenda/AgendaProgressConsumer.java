package com.comcast.pop.handler.executor.impl.progress.agenda;

import com.comcast.pop.api.progress.AgendaProgress;

public interface AgendaProgressConsumer
{
    void setAgendaProgress(AgendaProgress agendaProgress);
    void registerOperationProgressProvider(OperationProgressProvider operationProgressProvder);
    void setTotalProgressOperationCount(int operationTotalCount);
    void adjustTotalProgressOperationCount(int operationTotalCountAdjustment);
    void incrementCompletedOperationCount(int incrementAmount);
}
