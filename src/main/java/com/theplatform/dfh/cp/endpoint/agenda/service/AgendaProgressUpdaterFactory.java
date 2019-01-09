package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.client.DataObjectRequestProcessorClient;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;

public class AgendaProgressUpdaterFactory
{
    public AgendaProgressUpdater createAgendaProgressUpdater(ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister)
    {
        return new AgendaProgressUpdater(
            new DataObjectRequestProcessorClient<>(
                new AgendaProgressRequestProcessor(
                    agendaProgressPersister,
                    operationProgressPersister
                )
            )
        );
    }
}
