package com.theplatform.dfh.cp.agenda.reclaim.factory;

import com.theplatform.dfh.cp.agenda.reclaim.AgendaReclaimer;
import com.theplatform.dfh.cp.agenda.reclaim.config.ReclaimerConfig;

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
