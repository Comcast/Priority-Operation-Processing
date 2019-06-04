package com.theplatform.dfh.cp.modules.monitor.metric;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.SharedMetricRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Receives a metric register and registers reporters using a factory class for abstraction.
 * Allows for selective reporting as well as scheduled.
 */
public class MetricReport
{
    private MetricRegistry metricRegistry;
    private List<ScheduledReporter> reporters = new ArrayList<>();

    public MetricReport(MetricRegistry metricRegistry)
    {
        this.metricRegistry = metricRegistry == null ? SharedMetricRegistries.getOrCreate("default") : metricRegistry;
    }

    public MetricRegistry getMetricRegistry()
    {
        return metricRegistry;
    }

    public MetricReport register(MetricReporterFactory reporterFactory)
    {
        ScheduledReporter reporter = reporterFactory.register(metricRegistry);
        reporter.start(reporterFactory.getReportIntervalInMilli(), TimeUnit.MILLISECONDS);
        reporters.add(reporter);
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
}
