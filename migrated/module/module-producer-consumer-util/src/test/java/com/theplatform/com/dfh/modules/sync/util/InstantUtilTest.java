package com.theplatform.com.dfh.modules.sync.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;

public class InstantUtilTest
{
    private final Instant NOW = Instant.now();

    @DataProvider
    public Object[][] afterOrEqualProvider()
    {
        return new Object[][]
            {
                { NOW.plusSeconds(60), true},   // since time is the future (odd but shouldn't error out)
                { NOW.minusSeconds(1), false},  // almost a minute past
                { NOW.minusSeconds(60), false}, // almost a minute past
            };
    }

    @Test(dataProvider = "afterOrEqualProvider")
    public void testIsAfterOrEqual(Instant sourceTime, final boolean EXPECTED_RESPONSE)
    {
        Assert.assertEquals(EXPECTED_RESPONSE, InstantUtil.isAfterOrEqual(sourceTime, NOW));
    }

    @DataProvider
    public Object[][] nowAfterOrEqualProvider()
    {
        return new Object[][]
            {
                { NOW.plusSeconds(60), false},   // since time is the future (odd but shouldn't error out)
                { NOW.minusSeconds(2), true},  // almost a minute past
                { NOW.minusSeconds(60), true}, // almost a minute past
            };
    }

    @Test(dataProvider = "nowAfterOrEqualProvider")
    public void testIsNowAfterOrEqual(Instant instant, final boolean EXPECTED_RESPONSE)
    {
        Assert.assertEquals(EXPECTED_RESPONSE, InstantUtil.isNowAfterOrEqual(instant));
    }

    @Test
    public void testIsAfterOrEqualWithSameTime()
    {
        Assert.assertTrue(InstantUtil.isAfterOrEqual(NOW, NOW));
    }
}

