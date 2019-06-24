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
    private Meter failedMeter;
    private Meter deletedMeter;
    private Timer durationTimer;

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
    public MetricReporter withFailedMeter()
    {
        if(failedMeter == null)
            this.failedMeter = getMetricRegistry().meter(MetricLabel.failed.name());
       return this;
    }
    public MetricReporter withDeletedMeter()
    {
        if(deletedMeter == null)
            this.deletedMeter = getMetricRegistry().meter(MetricLabel.deleted.name());
        return this;
    }
    public MetricReporter withDurationTimer()
    {
        if(durationTimer == null)
            this.durationTimer = getMetricRegistry().timer(MetricLabel.duration.name());
        return this;
    }
    //The reporters are currently a scheduled reporter. In some cases, we want to report now.
    public void report()
    {
        for (ScheduledReporter reporter : reporters)
        {
            reporter.report();
        }
    }
    public Meter getFailedMeter()
    {
        return failedMeter;
    }
    public Meter getDeletedMeter()
    {
        return deletedMeter;
    }
    public Timer getTimer()
    {
        return durationTimer;
    }
}
