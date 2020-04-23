package com.comcast.pop.modules.sync.util;

import java.time.Instant;

public class InstantUtil
{
    /**
     * Determines if "now" is after or equal to the specified end Instant
     * @param endTime The time to compare now to
     * @return true if now is after or equal to the specified end Instant, false otherwise
     */
    public static boolean isNowAfterOrEqual(Instant endTime)
    {
        return isAfterOrEqual(Instant.now(), endTime);
    }

    /**
     * Determines if the source Instant is after or equal to the specified target Instant
     * @param source The source Instant
     * @param target The target Instant
     * @return true if source is after or equal to the specified target Instant, false otherwise
     */
    public static boolean isAfterOrEqual(Instant source, Instant target)
    {
        return source.equals(target)
            || source.isAfter(target);
    }
}
