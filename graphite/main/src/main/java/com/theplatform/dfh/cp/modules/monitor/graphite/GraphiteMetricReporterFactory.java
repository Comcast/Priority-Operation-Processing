package com.theplatform.dfh.cp.modules.monitor.graphite;

import com.codahale.metrics.*;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporterFactory;

import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Sends graphite metrics. Usage see: MetricReporter for registering.
 */
public class GraphiteMetricReporterFactory implements MetricReporterFactory
{
    private GraphiteConfiguration metricsConfiguration;
    private MetricFilter metricFilter = MetricFilter.ALL;

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
        GraphiteReporter reporter = GraphiteReporter.forRegistry(metricRegistry)
            .convertRatesTo(TimeUnit.MILLISECONDS)
            // N.B 1m is "somewhat" arbitrary - it controls the retention period of the metrics for comparision purposes
            .prefixedWith( metricsConfiguration.getGraphitePath() )
            .filter(metricFilter)
            .build(standardPusher);
        //We need to override the Graphite Reporter to catch all exceptions from preventing our applications to fail.
        //However, the reporter builder and instance is private, so we wrap it
        return new GraphiteReporterWrapper(metricRegistry, "graphite-reporter", MetricFilter.ALL, TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS, reporter);
    }

    @Override
    public int getReportIntervalInMilli()
    {
        return metricsConfiguration.getMetricsReportInterval();
    }

    public void setMetricFilter(MetricFilter metricFilter)
    {
        this.metricFilter = metricFilter;
    }
}
