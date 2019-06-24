package com.theplatform.dfh.cp.modules.monitor.metric;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;

import java.util.concurrent.TimeUnit;

public class LoggingMetricReporterFactory implements MetricReporterFactory
{
    private int reportIntervalInMilli = 30000;
    private MetricFilter metricFilter;

    public LoggingMetricReporterFactory()
    {
        this(MetricFilter.ALL);
    }
    public LoggingMetricReporterFactory(MetricFilter metricFilter)
    {
        this.metricFilter = metricFilter;
    }
    public LoggingMetricReporterFactory(int reportIntervalInMilli)
    {
        this.reportIntervalInMilli = reportIntervalInMilli;
    }

    @Override
    public ScheduledReporter register(MetricRegistry metricRegistry)
    {
        return Slf4jReporter.forRegistry(metricRegistry)
            .convertRatesTo(TimeUnit.MILLISECONDS)
            .filter(metricFilter)
            .build();
    }

    @Override
    public int getReportIntervalInMilli()
    {
        return reportIntervalInMilli;
    }

    public void setMetricFilter(MetricFilter metricFilter)
    {
        this.metricFilter = metricFilter;
    }
}
