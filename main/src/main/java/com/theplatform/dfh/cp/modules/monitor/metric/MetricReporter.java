package com.theplatform.dfh.cp.modules.monitor.metric;

import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Receives a metric register and registers reporters using a factory class for abstraction.
 * Allows for selective reporting as well as scheduled.
 */
public class MetricReporter
{
    private MetricRegistry metricRegistry;
    private List<ScheduledReporter> reporters = new ArrayList<>();
    private static Logger logger = LoggerFactory.getLogger(MetricReporter.class);


    public MetricReporter(MetricRegistry metricRegistry)
    {
        this.metricRegistry = metricRegistry == null ? SharedMetricRegistries.getOrCreate("metric-reporter") : metricRegistry;
    }
    public MetricReporter()
    {
        this(new MetricRegistry());
    }

    public MetricRegistry getMetricRegistry()
    {
        return metricRegistry;
    }

    public ScheduledReporter register(MetricReporterFactory reporterFactory)
    {
        try
        {

            ScheduledReporter reporter = reporterFactory.register(metricRegistry);
            if (reporter == null)
                return null;
            reporter.start(reporterFactory.getReportIntervalInMilli(), TimeUnit.MILLISECONDS);
            reporters.add(reporter);
            return reporter;
        }
        catch(Throwable e)
        {
            logger.error("Unable to register reporter." , e);
            return null;
        }
    }
    public void countInc(MetricLabel metricLabel)
    {
        countInc(metricLabel.name());
    }
    public void countInc(String metricLabel)
    {
        if(metricLabel == null) return;
        try
        {
            getMetricRegistry().counter(metricLabel).inc();
        }
        catch(Throwable e)
        {
            logger.error("Failure marking metric for reporting.", e);
        }
    }

    public void countDec(MetricLabel metricLabel)
    {
       countDec(metricLabel.name());
    }
    public void countDec(String metricLabel)
    {
        if(metricLabel == null) return;
        try
        {
            getMetricRegistry().counter(metricLabel).inc();
        }
        catch(Throwable e)
        {
            logger.error("Failure marking metric for reporting.", e);
        }
    }

    public void mark(MetricLabel metricLabel)
    {
        mark(metricLabel.name());
    }
    public void mark(String metricLabel)
    {
        if(metricLabel == null) return;
        try
        {
            getMetricRegistry().meter(metricLabel).mark();
        }
        catch(Throwable e)
        {
            logger.error("Failure marking metric for reporting.", e);
        }
    }
    public Timer.Context timerStart(MetricLabel metricLabel)
    {
        return timerStart(metricLabel.name());
    }
    public Timer.Context timerStart(String metricLabel)
    {
        if(metricLabel == null) return null;
        try
        {
            return getMetricRegistry().timer(metricLabel).time();
        }
        catch(Throwable e)
        {
            logger.error("Failure marking metric for reporting.", e);
        }
        return null;
    }

    //The reporters are currently a scheduled reporter. In some cases, we want to report now.
    public void report()
    {
        for (ScheduledReporter reporter : reporters)
        {
            reporter.report();
        }
    }

    public void close()
    {
        for (ScheduledReporter reporter : reporters)
        {
            try
            {
                reporter.close();
            }
            catch (Throwable e)
            {
                logger.error("Unable to close reporter.", e);
            }
        }
    }
}
