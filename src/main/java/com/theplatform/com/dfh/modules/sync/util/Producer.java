package com.theplatform.com.dfh.modules.sync.util;

import java.time.Instant;

public interface Producer<T>
{
    void reset();
    ProducerResult<T> produce(Instant endProcessingTime);
}
