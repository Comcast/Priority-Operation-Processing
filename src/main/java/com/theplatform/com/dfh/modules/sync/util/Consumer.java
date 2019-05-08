package com.theplatform.com.dfh.modules.sync.util;

import java.time.Instant;
import java.util.Collection;

public interface Consumer<T>
{
    /**
     * Performs the consume operation
     * @param input The input to operate on
     * @param endProcessingTime The UTC time that processing should stop at
     * @return A ConsumerResult
     */
    ConsumerResult<T> consume(Collection<T> input, Instant endProcessingTime);
}
