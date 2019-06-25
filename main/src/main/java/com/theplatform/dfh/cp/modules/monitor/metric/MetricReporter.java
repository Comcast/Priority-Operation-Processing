package com.theplatform.dfh.cp.modules.monitor.metric;

import com.codahale.metrics.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Receives a metric register and registers reporters using a factory class for abstraction.
 * Allows for selective reporting as well as scheduled.
 */
public class MetricReporter
{
    private MetricRegistry metricRegistry;
    private List<ScheduledReporter> reporters = new ArrayList<>();


    public MetricReporter(MetricRegistry metricRegistry)
    {
        this.metricRegistry = metricRegistry == null ? SharedMetricRegistries.getOrCreate("metric-reporter") : metricRegistry;
    }
    public MetricReporter()
    {
        this(null);
    }

    public MetricRegistry getMetricRegistry()
    {
        return metricRegistry;
    }

    public MetricReporter register(MetricReporterFactory reporterFactory)
    {
        ScheduledReporter reporter = reporterFactory.register(metricRegistry);
        reporter.start(reporterFactory.getReportIntervalInMilli(), TimeUnit.MILLISECONDS);
        reporters.add(reporter);
        return this;
    }
    public Counter getCounter(MetricLabel metricLabel)
    {
        if(metricLabel == null) return null;
        return getCounter(metricLabel.name());
    }
    public Meter getMeter(MetricLabel metricLabel)
    {
        if(metricLabel == null) return null;
        return getMeter(metricLabel.name());
    }
    public Counter getCounter(String metricLabel)
    {
        if(metricLabel == null) return null;
        return getMetricRegistry().counter(metricLabel);
    }
    public Meter getMeter(String metricLabel)
    {
        if(metricLabel == null) return null;
        return getMetricRegistry().meter(metricLabel);
    }
    public Timer getTimer(MetricLabel metricLabel)
    {
        if(metricLabel == null) return null;
        return getTimer(metricLabel.name());
    }
    public Timer getTimer(String metricLabel)
    {
        if(metricLabel == null) return null;
        return getMetricRegistry().timer(metricLabel);
    }

    //The reporters are currently a scheduled reporter. In some cases, we want to report now.
    public void report()
    {
        for (ScheduledReporter reporter : reporters)
        {
            reporter.report();
        }
    }
}
