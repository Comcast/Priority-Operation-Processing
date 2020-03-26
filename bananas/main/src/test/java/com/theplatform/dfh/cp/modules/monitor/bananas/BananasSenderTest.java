package com.theplatform.dfh.cp.modules.monitor.bananas;

import com.theplatform.dfh.cp.modules.monitor.alert.AlertException;
import com.theplatform.dfh.cp.modules.monitor.alert.AlertLevel;
import com.theplatform.dfh.cp.modules.monitor.bananas.message.BananasMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.SocketTimeoutException;

public class BananasSenderTest
{
    BananasMessage message = new BananasMessage();

    @Test
    public void testNoRetries()
    {
        Sender sender = new Sender(new SocketTimeoutException(), 0);
        sender.send(message);
        Assert.assertEquals(sender.getNumberOfInvocations(), 1);
    }

    @Test
    public void testOneRetry()
    {
        Sender sender = new Sender(new SocketTimeoutException(), 1);
        sender.send(message);
        Assert.assertEquals(sender.getNumberOfInvocations(), 2);
    }
    @Test(expectedExceptions = AlertException.class)
    public void testOneRetryNonRetriable()
    {
        Sender sender = new Sender(new RuntimeException(), 1);
        sender.send(message);
    }

    private class Sender extends BananasSender
    {
        int invocationCount = 0;
        Exception exceptionToThrow;

        public Sender(Exception exceptionToThrow, int retryCount)
        {
            super("myhost", 0, retryCount);
            this.exceptionToThrow = exceptionToThrow;
        }

        @Override
        protected Boolean sendMessage(AlertLevel level, String messagePayload) throws Exception
        {
            invocationCount ++;
            throw exceptionToThrow;
        }

        public int getNumberOfInvocations()
        {
            return invocationCount;
        }
    }
}