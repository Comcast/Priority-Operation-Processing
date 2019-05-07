package com.theplatform.com.dfh.modules.sync.util;

import java.time.Instant;
import java.util.Collection;

public interface Producer<T>
{
    void reset();
    Collection<T> produce(Instant endProcessingTime);
}
