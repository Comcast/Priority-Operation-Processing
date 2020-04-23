package com.comcast.pop.modules.monitor.metric;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.comcast.pop.modules.monitor.config.ConfigurationProperties;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class LoggingMetricReporterFactory implements MetricReporterFactory
{
    private MetricFilter metricFilter;
    private ConfigurationProperties configurationProperties;

    public LoggingMetricReporterFactory()
    {
        this(new Properties(), MetricFilter.ALL);
    }
    public LoggingMetricReporterFactory(ConfigurationProperties configurationProperties, MetricFilter metricFilter)
    {
        this.metricFilter = metricFilter;
        this.configurationProperties = configurationProperties;
    }
    public LoggingMetricReporterFactory(Properties properties, MetricFilter metricFilter)
    {
        this(ConfigurationProperties.from(properties, new LoggingConfigKeys()), metricFilter);
    }


    @Override
    public ScheduledReporter register(MetricRegistry metricRegistry)
    {
        return new ScheduledReporterWrapper<>(
            configurationProperties,
            Slf4jReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.MILLISECONDS)
                .filter(metricFilter)
                .build(),
            LoggingConfigKeys.ENABLED);
    }

    @Override
    public int getReportIntervalInMilli()
    {
        return configurationProperties.get(LoggingConfigKeys.REPORT_FREQUENCY);
    }

    public void setMetricFilter(MetricFilter metricFilter)
    {
        this.metricFilter = metricFilter;
    }
}
