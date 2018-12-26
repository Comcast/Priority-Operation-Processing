package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.endpoint.client.HttpCPObjectClient;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

public class AgendaProgressUpdaterFactory
{
    public AgendaProgressUpdater getAgendaProgressUpdater(HttpURLConnectionFactory httpURLConnectionFactory, String agendaProgressURL)
    {
        return new AgendaProgressUpdater(new HttpCPObjectClient<>(
            agendaProgressURL,
            httpURLConnectionFactory,
            AgendaProgress.class));
    }
}
