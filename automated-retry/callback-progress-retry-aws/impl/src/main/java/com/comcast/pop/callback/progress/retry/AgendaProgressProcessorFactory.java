package com.comcast.pop.callback.progress.retry;

import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.endpoint.client.AgendaServiceClient;
import com.comcast.pop.endpoint.client.ObjectClient;

public class AgendaProgressProcessorFactory
{
    public AgendaProgressProcessor createAgendaProgressProcessor(AgendaServiceClient agendaServiceClient, ObjectClient<AgendaProgress> agendaProgressClient)
    {
        return new AgendaProgressProcessor(agendaServiceClient, agendaProgressClient);
    }
}
