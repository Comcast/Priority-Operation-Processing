package com.theplatform.com.dfh.modules.sync.util;

import java.time.Instant;
import java.util.Collection;

public interface Consumer<T>
{
    ConsumerResult<T> consume(Collection<T> input, Instant endProcessingTime);
}
