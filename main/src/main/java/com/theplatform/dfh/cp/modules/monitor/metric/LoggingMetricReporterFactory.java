package com.theplatform.dfh.cp.modules.monitor.metric;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;

import java.util.concurrent.TimeUnit;

public class LoggingMetricReporterFactory implements MetricReporterFactory
{
    private int reportIntervalInMilli = 30000;

    public LoggingMetricReporterFactory()
    {
    }
    public LoggingMetricReporterFactory(int reportIntervalInMilli)
    {
        this.reportIntervalInMilli = reportIntervalInMilli;
    }

    @Override
    public ScheduledReporter register(MetricRegistry metricRegistry)
    {
        return register(metricRegistry, MetricFilter.ALL);
    }

    @Override
    public ScheduledReporter register(MetricRegistry metricRegistry, MetricFilter filterMetrics)
    {
        return Slf4jReporter.forRegistry(metricRegistry)
            .convertRatesTo(TimeUnit.MILLISECONDS)
            .filter(filterMetrics)
            .build();
    }

    @Override
    public int getReportIntervalInMilli()
    {
        return reportIntervalInMilli;
    }
}
