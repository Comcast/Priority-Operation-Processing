package com.theplatform.dfh.cp.agenda.reclaim.aws.producer;

import com.theplatform.com.dfh.modules.sync.util.Producer;
import com.theplatform.com.dfh.modules.sync.util.ProducerResult;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;

import java.time.Instant;

public class AgendaProgressTimeoutProducer implements Producer<AgendaProgress>
{
    @Override
    public void reset()
    {

    }

    @Override
    public ProducerResult<AgendaProgress> produce(Instant instant)
    {
        return null;
    }
}
