package com.theplatform.dfh.cp.handler.puller.impl.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.theplatform.dfh.cp.handler.puller.impl.context.ExecutionContext;

public class AliveHealthCheck extends HealthCheck
{
    private ExecutionContext executionContext;

    public AliveHealthCheck(ExecutionContext executionContext)
    {
        this.executionContext = executionContext;
    }

    @Override
    protected Result check() throws Exception
    {
        return executionContext.isThreadAlive() ? Result.healthy() : Result.unhealthy("Puller thread is not alive.");
    }
}