package com.comcast.pop.agenda.reclaim.factory;

import com.comcast.pop.agenda.reclaim.consumer.AgendaProgressTimeoutConsumer;
import com.comcast.pop.modules.sync.util.Consumer;
import com.comcast.pop.agenda.reclaim.config.ReclaimerConfig;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.endpoint.client.HttpObjectClient;
import com.comcast.pop.http.api.HttpURLConnectionFactory;

public class TimeoutConsumerFactory implements AgendaProgressConsumerFactory
{
    private final HttpURLConnectionFactory httpURLConnectionFactory;
    private final String agendaProgressURL;
    private final ReclaimerConfig config;

    public TimeoutConsumerFactory(
        HttpURLConnectionFactory httpURLConnectionFactory,
        ReclaimerConfig config,
        String agendaProgressURL)
    {
        this.httpURLConnectionFactory = httpURLConnectionFactory;
        this.config = config;
        this.agendaProgressURL = agendaProgressURL;
    }

    @Override
    public Consumer<String> create()
    {
        HttpObjectClient<AgendaProgress> agendaProgressClient = new HttpObjectClient<>(agendaProgressURL, httpURLConnectionFactory, AgendaProgress.class);
        return new AgendaProgressTimeoutConsumer(agendaProgressClient)
            .setLogReclaimOnly(config.getLogReclaimOnly());
    }
}
