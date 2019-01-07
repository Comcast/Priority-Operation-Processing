package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.adapter.client.RequestProcessorAdapter;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;

public class AgendaProgressUpdaterFactory
{
    public AgendaProgressUpdater createAgendaProgressUpdater(ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister)
    {
        return new AgendaProgressUpdater(
            new RequestProcessorAdapter<>(
                new AgendaProgressRequestProcessor(
                    agendaProgressPersister,
                    operationProgressPersister
                )
            )
        );
    }
}
