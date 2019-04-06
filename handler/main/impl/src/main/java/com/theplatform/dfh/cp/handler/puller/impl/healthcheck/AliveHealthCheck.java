package com.theplatform.dfh.cp.handler.puller.impl.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.theplatform.dfh.cp.handler.puller.impl.context.ExecutionContext;
import com.theplatform.dfh.cp.handler.puller.impl.monitor.alive.AliveCheck;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AliveHealthCheck extends HealthCheck
{
    private ExecutionContext executionContext;

    private List<AliveCheck> aliveChecks = new LinkedList<>();

    public AliveHealthCheck(ExecutionContext executionContext)
    {
        this.executionContext = executionContext;
    }

    @Override
    protected Result check()
    {
        String notAliveMessage = aliveChecks.stream()
            .filter(aliveCheck -> !aliveCheck.isAlive())
            .map(AliveCheck::getNotAliveString)
            .collect(Collectors.joining(", "));
        if(StringUtils.isNotBlank(notAliveMessage)) return Result.unhealthy(notAliveMessage);

        return executionContext.isThreadAlive() ? Result.healthy() : Result.unhealthy("Puller thread is not alive.");
    }

    public AliveHealthCheck addAliveCheck(AliveCheck aliveCheck)
    {
        if(aliveCheck != null) aliveChecks.add(aliveCheck);
        return this;
    }
}