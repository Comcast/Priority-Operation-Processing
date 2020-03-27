package com.comcast.fission.agenda.reclaim.factory;

import com.comcast.fission.agenda.reclaim.AgendaReclaimer;
import com.comcast.fission.agenda.reclaim.config.ReclaimerConfig;

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
