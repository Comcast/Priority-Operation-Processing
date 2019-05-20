package com.theplatform.dfh.cp.agenda.reclaim;

import com.theplatform.com.dfh.modules.sync.util.SynchronousProducerConsumerProcessor;
import com.theplatform.dfh.cp.agenda.reclaim.config.ReclaimerConfig;
import com.theplatform.dfh.cp.agenda.reclaim.factory.AgendaProgressConsumerFactory;
import com.theplatform.dfh.cp.agenda.reclaim.factory.AgendaProgressProducerFactory;

public class AgendaReclaimer
{
    private SynchronousProducerConsumerProcessor<String> processor;

    public AgendaReclaimer(AgendaProgressProducerFactory producerFactory, AgendaProgressConsumerFactory consumerFactory, ReclaimerConfig config)
    {
        processor =
            new SynchronousProducerConsumerProcessor<>(
                producerFactory.create(),
                consumerFactory.create()
            )
            .setRunMaxSeconds(config.getMaximumExecutionSeconds());
    }

    public void process()
    {
        this.processor.execute();
    }

    protected void setProcessor(SynchronousProducerConsumerProcessor<String> processor)
    {
        this.processor = processor;
    }
}
