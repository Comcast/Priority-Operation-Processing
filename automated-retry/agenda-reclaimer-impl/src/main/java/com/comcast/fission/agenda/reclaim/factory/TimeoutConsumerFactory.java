package com.comcast.fission.agenda.reclaim.factory;

import com.comcast.fission.agenda.reclaim.consumer.AgendaProgressTimeoutConsumer;
import com.theplatform.com.dfh.modules.sync.util.Consumer;
import com.comcast.fission.agenda.reclaim.config.ReclaimerConfig;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

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
