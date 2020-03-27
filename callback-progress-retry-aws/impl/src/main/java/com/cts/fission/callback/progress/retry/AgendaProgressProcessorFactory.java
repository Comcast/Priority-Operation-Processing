package com.cts.fission.callback.progress.retry;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.endpoint.client.AgendaServiceClient;
import com.theplatform.dfh.endpoint.client.ObjectClient;

public class AgendaProgressProcessorFactory
{
    public AgendaProgressProcessor createAgendaProgressProcessor(AgendaServiceClient agendaServiceClient, ObjectClient<AgendaProgress> agendaProgressClient)
    {
        return new AgendaProgressProcessor(agendaServiceClient, agendaProgressClient);
    }
}
