package com.theplatform.com.dfh.modules.pcutil;

import java.time.Instant;
import java.util.Collection;

public interface Consumer<T>
{
    ConsumerResult<T> consume(Collection<T> input, Instant endProcessingTime);
}
