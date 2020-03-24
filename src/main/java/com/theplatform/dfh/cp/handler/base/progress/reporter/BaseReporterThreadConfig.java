package com.theplatform.dfh.cp.handler.base.progress.reporter;

/**
 * Base configuration for a reporter thread
 * @param <T> The type of the config (allows for setters to return the type)
 */
public abstract class BaseReporterThreadConfig<T extends BaseReporterThreadConfig>
{
    protected int updateIntervalMilliseconds = 5000;
    protected int maxReportAttemptsAfterShutdown = 5;

    public int getUpdateIntervalMilliseconds()
    {
        return updateIntervalMilliseconds;
    }

    public T setUpdateIntervalMilliseconds(int updateIntervalMilliseconds)
    {
        this.updateIntervalMilliseconds = updateIntervalMilliseconds;
        return (T)this;
    }

    public int getMaxReportAttemptsAfterShutdown()
    {
        return maxReportAttemptsAfterShutdown;
    }

    public T setMaxReportAttemptsAfterShutdown(int maxReportAttemptsAfterShutdown)
    {
        this.maxReportAttemptsAfterShutdown = maxReportAttemptsAfterShutdown;
        return (T)this;
    }
}
