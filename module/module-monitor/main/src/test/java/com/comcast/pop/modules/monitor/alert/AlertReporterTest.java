package com.comcast.pop.modules.monitor.alert;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AlertReporterTest
{
    AlertSender sender = Mockito.mock(AlertSender.class);
    AlertMessage message = Mockito.mock(AlertMessage.class);

    @Test
    public void testZeroThreshold()
    {
        AlertReporter reporter = new AlertReporter(message, sender, 1);
        reporter.markFailed();

        Assert.assertEquals(reporter.getAlertCount(), 0);
        Assert.assertEquals(reporter.isAlerting(), true);
        Mockito.verify(message, Mockito.atLeastOnce()).setLevel(Mockito.eq(AlertLevel.INFO));
        Mockito.verify(sender, Mockito.atLeastOnce()).send(Mockito.any());
    }
    @Test
    public void testNegativeThreshold()
    {
        //defaults to 2
        AlertReporter reporter = new AlertReporter(message, sender, -1);
        reporter.markFailed();
        reporter.markFailed();
        //should be back to zero after alerting.
        Assert.assertEquals(reporter.getAlertCount(), 0);
        Assert.assertEquals(reporter.isAlerting(), true);
        Mockito.verify(message, Mockito.atLeastOnce()).setLevel(Mockito.eq(AlertLevel.INFO));
        Mockito.verify(sender, Mockito.atLeastOnce()).send(Mockito.any());
    }
    @Test
    public void testPassBeforeThreshold()
    {
        //defaults to 2
        AlertReporter reporter = new AlertReporter(message, sender, 2);
        reporter.markFailed();
        //not enough to alert, yet
        Assert.assertEquals(reporter.getAlertCount(), 1);
        Assert.assertEquals(reporter.isAlerting(), false);

        reporter.markPassed();
        //should be back to zero after alerting.
        Assert.assertEquals(reporter.getAlertCount(), 0);
        Assert.assertEquals(reporter.isAlerting(), false);

        reporter.markFailed();
        //not enough to alert, yet
        Assert.assertEquals(reporter.getAlertCount(), 1);
        Assert.assertEquals(reporter.isAlerting(), false);
        reporter.markFailed();
        Assert.assertEquals(reporter.isAlerting(), true);
        Mockito.verify(message, Mockito.atLeastOnce()).setLevel(Mockito.eq(AlertLevel.INFO));
        Mockito.verify(sender, Mockito.atLeastOnce()).send(Mockito.any());
    }
    @Test
    public void testFailureLevel()
    {
        //defaults to 2
        AlertReporter reporter = new AlertReporter(message, sender, 1);
        reporter.setAlertFailedLevel(AlertLevel.CRITICAL);
        reporter.markFailed();
        //should be back to zero after alerting.
        Assert.assertEquals(reporter.getAlertCount(), 0);
        Assert.assertEquals(reporter.isAlerting(), true);
        Mockito.verify(message, Mockito.atLeastOnce()).setLevel(Mockito.eq(AlertLevel.CRITICAL));
        Mockito.verify(sender, Mockito.atLeastOnce()).send(Mockito.any());
    }
    @Test
    public void testPassLevel()
    {
        //defaults to 2
        AlertReporter reporter = new AlertReporter(message, sender, 1);
        reporter.setAlertPassedLevel(AlertLevel.WARNING);
        reporter.markFailed();
         //should be back to zero after alerting.
        Assert.assertEquals(reporter.getAlertCount(), 0);
        Assert.assertEquals(reporter.isAlerting(), true);
        reporter.markPassed();
        Assert.assertEquals(reporter.getAlertCount(), 0);
        Assert.assertEquals(reporter.isAlerting(), false);

        Mockito.verify(message, Mockito.atLeastOnce()).setLevel(Mockito.eq(AlertLevel.WARNING));
        Mockito.verify(sender, Mockito.atLeastOnce()).send(Mockito.any());
    }
}
