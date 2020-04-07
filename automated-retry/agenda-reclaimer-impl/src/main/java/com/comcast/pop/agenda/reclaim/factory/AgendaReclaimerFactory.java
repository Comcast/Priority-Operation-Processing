package com.comcast.pop.agenda.reclaim.factory;

import com.comcast.pop.agenda.reclaim.AgendaReclaimer;
import com.comcast.pop.agenda.reclaim.config.ReclaimerConfig;

public class AgendaReclaimerFactory
{
    public AgendaReclaimer createAgendaReclaimer(AgendaProgressProducerFactory producerFactory, AgendaProgressConsumerFactory consumerFactory, ReclaimerConfig config)
    {
        return new AgendaReclaimer(
            producerFactory,
            consumerFactory,
            config
        );
    }
}
