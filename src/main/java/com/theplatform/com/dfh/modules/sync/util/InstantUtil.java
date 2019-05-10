package com.theplatform.com.dfh.modules.sync.util;

import java.time.Instant;

public class InstantUtil
{
    public static boolean isNowAfterOrEqual(Instant endTime)
    {
        return isAfterOrEqual(Instant.now(), endTime);
    }

    public static boolean isAfterOrEqual(Instant source, Instant target)
    {
        return source.equals(target)
            || source.isAfter(target);
    }
}
