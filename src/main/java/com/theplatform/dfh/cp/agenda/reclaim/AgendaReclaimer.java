package com.theplatform.dfh.cp.agenda.reclaim;

import com.theplatform.com.dfh.modules.sync.util.SynchronousProducerConsumerProcessor;
import com.theplatform.dfh.cp.agenda.reclaim.config.ReclaimerConfig;
import com.theplatform.dfh.cp.agenda.reclaim.factory.AgendaProgressConsumerFactory;
import com.theplatform.dfh.cp.agenda.reclaim.factory.AgendaProgressProducerFactory;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;

public class AgendaReclaimer
{
    private SynchronousProducerConsumerProcessor<AgendaProgress> processor;

    public AgendaReclaimer(AgendaProgressProducerFactory producerFactory, AgendaProgressConsumerFactory consumerFactory, ReclaimerConfig config)
    {
        this.processor =
            new SynchronousProducerConsumerProcessor<>(
                producerFactory.create(config),
                consumerFactory.create(config)
            )
            .setRunMaxSeconds(config.getMaxRunSeconds());
    }

    public void process()
    {
        this.processor.execute();
    }

    protected void setProcessor(SynchronousProducerConsumerProcessor<AgendaProgress> processor)
    {
        this.processor = processor;
    }
}
