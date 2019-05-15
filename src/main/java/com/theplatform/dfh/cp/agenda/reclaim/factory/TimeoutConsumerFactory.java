package com.theplatform.dfh.cp.agenda.reclaim.factory;

import com.theplatform.com.dfh.modules.sync.util.Consumer;
import com.theplatform.dfh.cp.agenda.reclaim.config.ReclaimerConfig;
import com.theplatform.dfh.cp.agenda.reclaim.consumer.AgendaProgressTimeoutConsumer;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

public class TimeoutConsumerFactory implements AgendaProgressConsumerFactory
{
    private HttpURLConnectionFactory httpURLConnectionFactory;

    public TimeoutConsumerFactory(HttpURLConnectionFactory httpURLConnectionFactory)
    {
        this.httpURLConnectionFactory = httpURLConnectionFactory;
    }

    @Override
    public Consumer<AgendaProgress> create(ReclaimerConfig config)
    {
        HttpObjectClient<AgendaProgress> agendaProgressClient = new HttpObjectClient<>(config.getAgendaProgressEndpointURL(), httpURLConnectionFactory, AgendaProgress.class);
        return new AgendaProgressTimeoutConsumer(agendaProgressClient);
    }
}
