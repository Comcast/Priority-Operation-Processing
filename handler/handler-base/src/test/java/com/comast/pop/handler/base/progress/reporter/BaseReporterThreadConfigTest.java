package com.comast.pop.handler.base.progress.reporter;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BaseReporterThreadConfigTest
{
    @Test
    public void testSetters()
    {
        // just a paranoia check for the templating stuff
        final int MAX_ATTEMPTS = 10;
        final int INTERVAL = 45;

        TestConfig testConfig = new TestConfig();
        testConfig = testConfig.setMaxReportAttemptsAfterShutdown(MAX_ATTEMPTS).setUpdateIntervalMilliseconds(INTERVAL);
        Assert.assertEquals(testConfig.getMaxReportAttemptsAfterShutdown(), MAX_ATTEMPTS);
        Assert.assertEquals(testConfig.getUpdateIntervalMilliseconds(), INTERVAL);
    }
}
