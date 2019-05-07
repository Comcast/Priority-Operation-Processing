package com.theplatform.com.dfh.modules.pcutil;

import java.time.Instant;
import java.util.Collection;

public interface Producer<T>
{
    void reset();
    Collection<T> produce(Instant endProcessingTime);
}
