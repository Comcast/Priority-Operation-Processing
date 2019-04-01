package com.theplatform.dfh.cp.modules.kube.fabric8.client.facade;

import net.jodah.failsafe.RetryPolicy;

import java.time.Duration;
import java.util.List;

public abstract class RetryableBase
{
    private int delaySeconds;
    private int attemptCount;
    private List<Class<? extends Throwable>> retryableExceptions;

    public RetryableBase(int delaySeconds, int attemptCount, List<Class<? extends Throwable>> retryableExceptions)
    {
        this.delaySeconds = delaySeconds;
        this.attemptCount = attemptCount;
        this.retryableExceptions = retryableExceptions;
    }

    protected <R> RetryPolicy<R> getRetryPolicy()
    {
        RetryPolicy<R> retryPolicy = new RetryPolicy<>();
        retryPolicy.handle(retryableExceptions);
        retryPolicy.withMaxAttempts(attemptCount);
        if(delaySeconds > 0) retryPolicy.withDelay(Duration.ofSeconds(delaySeconds));
        return retryPolicy;
    }

    public void setRetryableExceptions(List<Class<? extends Throwable>> retryableExceptions)
    {
        this.retryableExceptions = retryableExceptions;
    }

    public void setDelaySeconds(int delaySeconds)
    {
        this.delaySeconds = delaySeconds;
    }

    public void setAttemptCount(int attemptCount)
    {
        this.attemptCount = attemptCount;
    }
}
