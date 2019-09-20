package com.theplatform.dfh.cp.modules.monitor.graphite;

import com.codahale.metrics.*;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporterFactory;
import com.theplatform.dfh.cp.modules.monitor.metric.ScheduledReporterWrapper;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Sends graphite metrics. Usage see: MetricReporter for registering.
 */
public class GraphiteMetricReporterFactory implements MetricReporterFactory
{
    private ConfigurationProperties metricsConfiguration;
    private MetricFilter metricFilter = MetricFilter.ALL;

    public GraphiteMetricReporterFactory(Properties properties)
    {
        this.metricsConfiguration = ConfigurationProperties.from(properties, new GraphiteConfigKeys());
    }
    public GraphiteMetricReporterFactory(ConfigurationProperties configuration)
    {
        this.metricsConfiguration = configuration;
    }

    @Override
    public ScheduledReporter register(MetricRegistry metricRegistry)
    {
        // set up "standard" graphite push for metrics
        final String endpoint = metricsConfiguration.get(GraphiteConfigKeys.ENDPOINT);
        if(endpoint == null)
            throw new IllegalArgumentException("No endpoint defined for Graphite reporter. Missing property " +GraphiteConfigKeys.ENDPOINT.getPropertyKey());

        final Integer port = metricsConfiguration.get(GraphiteConfigKeys.PORT);

        InetSocketAddress metricsServerLocation = new InetSocketAddress(endpoint, port);
        if(metricsServerLocation.getAddress() == null)
            throw new RuntimeException(String.format("Unable to get socket address for host %s port %d to report Graphite metrics.", endpoint, port));
        Graphite standardPusher = new Graphite(metricsServerLocation);
        GraphiteReporter reporter = GraphiteReporter.forRegistry(metricRegistry)
            .convertRatesTo(TimeUnit.MILLISECONDS)
            // N.B 1m is "somewhat" arbitrary - it controls the retention period of the metrics for comparision purposes
            .prefixedWith(metricsConfiguration.get(GraphiteConfigKeys.PATH))
            .filter(metricFilter)
            .build(standardPusher);
        //We need to override the Graphite Reporter to catch all exceptions from preventing our applications to fail.
        //However, the reporter builder and instance is private, so we wrap it
        return new ScheduledReporterWrapper<>(
            metricsConfiguration,
            reporter,
            GraphiteConfigKeys.ENABLED);
    }

    @Override
    public int getReportIntervalInMilli()
    {
        return metricsConfiguration.get(GraphiteConfigKeys.REPORT_FREQUENCY);
    }

    public void setMetricFilter(MetricFilter metricFilter)
    {
        this.metricFilter = metricFilter;
    }
}
