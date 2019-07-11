package com.theplatform.dfh.cp.modules.monitor.metric;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

public class LoggingMetricReporterFactory implements MetricReporterFactory
{
    private int reportIntervalInMilli;
    private MetricFilter metricFilter;

    public LoggingMetricReporterFactory()
    {
        this(LoggingConfigKeys.REPORT_FREQUENCY.getDefaultValue(), MetricFilter.ALL);
    }
    public LoggingMetricReporterFactory(ConfigurationProperties configurationProperties, MetricFilter metricFilter)
    {
        this(configurationProperties != null ? configurationProperties.get(LoggingConfigKeys.REPORT_FREQUENCY) : null, metricFilter);
    }
    public LoggingMetricReporterFactory(Integer reportIntervalInMilli, MetricFilter metricFilter)
    {
        this.metricFilter = metricFilter;
        this.reportIntervalInMilli = reportIntervalInMilli == null ? LoggingConfigKeys.REPORT_FREQUENCY.getDefaultValue() : reportIntervalInMilli;
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
