package com.theplatform.dfh.cp.handler.reaper.impl.util;

import java.time.Duration;
import java.time.Instant;

public class InstantUtil
{
    public static boolean haveMinutesPassedSince(Instant start, Instant end, int minutes)
    {
        Duration duration = Duration.between(start, end);
        long differenceInSeconds = duration.minusMinutes(minutes).getSeconds();
        return differenceInSeconds >= 0;
    }
}
