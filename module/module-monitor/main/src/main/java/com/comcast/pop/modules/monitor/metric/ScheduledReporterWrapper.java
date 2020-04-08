package com.comcast.pop.modules.monitor.metric;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.comcast.pop.modules.monitor.config.ConfigKey;
import com.comcast.pop.modules.monitor.config.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper for a ScheduledReporter
 * @param <T> The type of ScheduledReporter wrapped
 */
public class ScheduledReporterWrapper<T extends ScheduledReporter> extends ScheduledReporter
{
    private static final Logger logger = LoggerFactory.getLogger(ScheduledReporterWrapper.class);

    private final T scheduledReporter;
    private final ConfigKey<Boolean> enabledKey;
    private final ConfigurationProperties configurationProperties;

    public ScheduledReporterWrapper(ConfigurationProperties configurationProperties, T scheduledReporter, ConfigKey<Boolean> enabledKey)
    {
        // TODO: all this could go away if the ScheduledReporter was an interface
        super(null, ScheduledReporterWrapper.class.getSimpleName(), null, TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS);
        this.configurationProperties = configurationProperties;
        this.scheduledReporter = scheduledReporter;
        this.enabledKey = enabledKey;
    }

    public T getScheduledReporter()
    {
        return scheduledReporter;
    }

    @Override
    public void start(long period, TimeUnit unit)
    {
        scheduledReporter.start(period, unit);
    }

    @Override
    public void stop()
    {
        scheduledReporter.stop();
    }

    @Override
    public void close()
    {
        scheduledReporter.close();
    }

    @Override
    public void report()
    {
        safeReport(() ->
        {
            scheduledReporter.report();
            // due to the callable type we return
            return null;
        });
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters,
        SortedMap<String, Timer> timers)
    {
        safeReport(() ->
        {
            scheduledReporter.report(gauges, counters, histograms, meters, timers);
            // due to the callable type we return
            return null;
        });
    }

    /**
     * Safely performs the specified call if the reporter is enabled
     * @param callable the call to perform
     */
    protected void safeReport(Callable<Void> callable)
    {
        if (configurationProperties != null && configurationProperties.get(enabledKey))
        {
            try
            {
                callable.call();
            }
            catch (Throwable e)
            {
                //don't crash our application if we can't send alerts.
                logger.error("Error occurred trying to get/send metrics via {}. Ignoring until next check.", scheduledReporter.getClass().getSimpleName(), e);
            }
        }
    }
}
