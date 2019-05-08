package com.theplatform.com.dfh.modules.sync.util;

import java.time.Instant;

public class InstantUtil
{
    public static boolean isEqualOrAfter(Instant endTime)
    {
        return Instant.now().equals(endTime)
            || Instant.now().isAfter(endTime);
    }
}
