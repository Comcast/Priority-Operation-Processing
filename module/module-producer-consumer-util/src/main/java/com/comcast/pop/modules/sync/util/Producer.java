package com.comcast.pop.modules.sync.util;

import java.time.Instant;

public interface Producer<T>
{
    /**
     * Resets the producer (any pagination, etc).
     */
    void reset();

    /**
     * Performs the produce operation
     * @param endProcessingTime The UTC time that processing should stop at
     * @return A ProducerResult
     */
    ProducerResult<T> produce(Instant endProcessingTime);
}
