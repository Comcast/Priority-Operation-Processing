package com.theplatform.dfh.cp.modules.monitor.graphite;

import com.codahale.metrics.*;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporterFactory;

import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Sends graphite metrics. Usage see: MetricReport for registering.
 */
public class GraphiteMetricReporterFactory implements MetricReporterFactory
{
    private GraphiteConfiguration metricsConfiguration;

    public GraphiteMetricReporterFactory(Properties properties)
    {
        this.metricsConfiguration = new GraphiteConfiguration(properties);
    }
    public GraphiteMetricReporterFactory(GraphiteConfiguration configuration)
    {
        this.metricsConfiguration = configuration;
    }

    @Override
    public ScheduledReporter register(MetricRegistry metricRegistry)
    {
        // set up "standard" graphite push for metrics
        InetSocketAddress metricsServerLocation =
            new InetSocketAddress( metricsConfiguration.getGraphiteEndpoint(), metricsConfiguration.getGraphitePort() );
        Graphite standardPusher = new Graphite(metricsServerLocation);
        return GraphiteReporter.forRegistry(metricRegistry)
            .convertRatesTo(TimeUnit.MILLISECONDS)
            // N.B 1m is "somewhat" arbitrary - it controls the retention period of the metrics for comparision purposes
            .prefixedWith( metricsConfiguration.getGraphitePath() )
            .build(standardPusher);
    }

    @Override
    public int getReportIntervalInMilli()
    {
        return metricsConfiguration.getMetricsReportInterval();
    }
}
