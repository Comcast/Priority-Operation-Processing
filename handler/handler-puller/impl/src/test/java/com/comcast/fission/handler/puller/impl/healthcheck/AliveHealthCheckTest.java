package com.comcast.fission.handler.puller.impl.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.comcast.fission.handler.puller.impl.context.ExecutionContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AliveHealthCheckTest
{
    private ExecutionContext executionContext = mock(ExecutionContext.class);

    @Test
    public void testThreadRunning()
    {
        AliveHealthCheck aliveHealthCheck = new AliveHealthCheck(executionContext);
        when(executionContext.isThreadAlive()).thenReturn(true);
        HealthCheck.Result result = aliveHealthCheck.execute();
        Assert.assertTrue(result.isHealthy());
    }

    @Test
    public void testThreadNotRunning()
    {
        AliveHealthCheck aliveHealthCheck = new AliveHealthCheck(executionContext);
        when(executionContext.isThreadAlive()).thenReturn(false);
        HealthCheck.Result result = aliveHealthCheck.execute();
        Assert.assertFalse(result.isHealthy());
    }
}
