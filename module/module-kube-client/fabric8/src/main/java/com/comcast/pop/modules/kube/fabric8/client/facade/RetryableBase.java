package com.comcast.pop.modules.kube.fabric8.client.facade;

import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public abstract class RetryableBase
{
    private static final Logger logger = LoggerFactory.getLogger(RetryableBase.class);
    private int maxSeconds = 60;
    private int delaySeconds;
    private int attemptCount;
    boolean useBackoff = false;
    private List<Class<? extends Throwable>> retryableExceptions;

    public RetryableBase(int maxSeconds, int delaySeconds, int attemptCount, boolean useBackoff, List<Class<? extends Throwable>> retryableExceptions)
    {
        this.maxSeconds = maxSeconds;
        this.delaySeconds = delaySeconds;
        this.attemptCount = attemptCount;
        this.useBackoff = useBackoff;
        this.retryableExceptions = retryableExceptions;
    }

    public RetryableBase(int delaySeconds, int attemptCount, boolean useBackoff, List<Class<? extends Throwable>> retryableExceptions)
    {
        this.delaySeconds = delaySeconds;
        this.attemptCount = attemptCount;
        this.useBackoff = useBackoff;
        this.retryableExceptions = retryableExceptions;
    }

    public RetryableBase(int delaySeconds, int attemptCount, List<Class<? extends Throwable>> retryableExceptions)
    {
        this.delaySeconds = delaySeconds;
        this.attemptCount = attemptCount;
        this.retryableExceptions = retryableExceptions;
    }

    protected <R> RetryPolicy<R> getRetryPolicy()
    {
        return getRetryPolicy(null);
    }

    /**
     * Gets the retry policy
     * @param attemptFailMessage The optional message to log when an attempt fails
     * @param <R>
     * @return A new retry policy
     */
    protected <R> RetryPolicy<R> getRetryPolicy(String attemptFailMessage)
    {
        RetryPolicy<R> retryPolicy = new RetryPolicy<>();
        retryPolicy.handle(retryableExceptions);
        retryPolicy.withMaxAttempts(attemptCount);
        if(attemptFailMessage != null)
            retryPolicy.onFailedAttempt(executionAttemptedEvent -> {
                logger.warn(attemptFailMessage, executionAttemptedEvent.getLastFailure());
            });
        if(delaySeconds > 0)
        {
            if(useBackoff)
                retryPolicy.withBackoff(delaySeconds, maxSeconds, ChronoUnit.SECONDS);
            else
                retryPolicy.withDelay(Duration.ofSeconds(delaySeconds));
        }
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

    public void setMaxSeconds(int maxSeconds)
    {
        this.maxSeconds = maxSeconds;
    }

    public void setUseBackoff(boolean useBackoff)
    {
        this.useBackoff = useBackoff;
    }
}
