package com.theplatform.dfh.cp.handler.reaper.impl.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;

public class InstantUtilTest
{
    private final Instant now = Instant.now();

    @DataProvider
    public Object[][] haveMinutesPassedSinceTestProvider()
    {
        return new Object[][]
        {
            {now.plusSeconds(60), 1, false},    // since time is the future (odd but shouldn't error out)
            {now, 1, false},                    // now
            {now.minusSeconds(59), 1, false},   // almost a minute past
            {now.minusSeconds(60), 1, true},    // exactly a minute past
            {now.minusSeconds(60 * 50), 45, true},
            {now.minusSeconds(60 * 50), 50, true},
        };
    }

    @Test(dataProvider = "haveMinutesPassedSinceTestProvider")
    public void testHaveMinutesPassedSince(Instant sinceInstant, int minutesToCheck, final boolean EXPECTED_RESPONSE)
    {
        Assert.assertEquals(EXPECTED_RESPONSE, InstantUtil.haveMinutesPassedSince(sinceInstant, now, minutesToCheck));
    }
}
