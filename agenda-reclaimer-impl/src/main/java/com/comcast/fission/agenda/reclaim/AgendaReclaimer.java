package com.comcast.fission.agenda.reclaim;

import com.comcast.fission.agenda.reclaim.config.ReclaimerConfig;
import com.comcast.fission.agenda.reclaim.factory.AgendaProgressConsumerFactory;
import com.comcast.fission.agenda.reclaim.factory.AgendaProgressProducerFactory;
import com.theplatform.com.dfh.modules.sync.util.SynchronousProducerConsumerProcessor;

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
