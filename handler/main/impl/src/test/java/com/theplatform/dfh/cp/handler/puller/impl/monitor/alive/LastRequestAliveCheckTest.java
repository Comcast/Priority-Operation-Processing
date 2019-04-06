package com.theplatform.dfh.cp.handler.puller.impl.monitor.alive;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LastRequestAliveCheckTest
{
    private LastRequestAliveCheck aliveCheck;

    @BeforeMethod
    public void setup()
    {
        aliveCheck = new LastRequestAliveCheck();
        aliveCheck.setNotAliveThresholdMilliseconds(1000L);
    }

    @Test
    public void testIsAliveUnSet() throws Exception
    {
        Assert.assertTrue(aliveCheck.isAlive());
    }

    @Test
    public void testIsAlive() throws Exception
    {
        aliveCheck.updateLastRequestDate();
        Assert.assertTrue(aliveCheck.isAlive());
    }

    @Test
    public void testIsNotAlive() throws Exception
    {
        aliveCheck.updateLastRequestDate();
        aliveCheck.setNotAliveThresholdMilliseconds(100L);
        Thread.sleep(1000);
        Assert.assertFalse(aliveCheck.isAlive());
    }
}
