package com.theplatform.dfh.cp.agenda.reclaim.aws.producer;

import com.theplatform.com.dfh.modules.sync.util.Producer;
import com.theplatform.dfh.cp.agenda.reclaim.config.ReclaimerConfig;
import com.theplatform.dfh.cp.agenda.reclaim.factory.AgendaProgressProducerFactory;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;

public class TimeoutProducerFactory implements AgendaProgressProducerFactory
{
    @Override
    public Producer<AgendaProgress> create(ReclaimerConfig config)
    {
        return new AgendaProgressTimeoutProducer();
    }
}
