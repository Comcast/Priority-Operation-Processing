package com.comcast.pop.handler.puller.impl;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

/**
 */
public class PullerExecutionTest
{

    @Test
    public void testAbort() throws InterruptedException
    {
        PullerEntryPoint entryPointMock = mock(PullerEntryPoint.class);

        PullerExecution pullerExecution = new PullerExecution(entryPointMock);
        pullerExecution.start();

        Thread.sleep(1000);

        pullerExecution.getExecutionContext().stopThread();
        Thread.sleep(1000);
        Assert.assertFalse(pullerExecution.getExecutionContext().isThreadAlive());
    }
}
