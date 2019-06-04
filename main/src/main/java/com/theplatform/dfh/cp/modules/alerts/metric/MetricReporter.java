package com.theplatform.dfh.cp.modules.alerts.metric;

import com.codahale.metrics.*;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.comcast.cts.timeshifted.pump.configuration.MetricsConfiguration;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MetricReporter
{
    private MetricsConfiguration metricsConfiguration;
    private MetricRegistry metricRegistry;
    private List<ScheduledReporter> reporters = new ArrayList<>();

    public MetricReporter(MetricsConfiguration metricsConfiguration, MetricRegistry metricRegistry)
    {
        this.metricRegistry = metricRegistry == null ? SharedMetricRegistries.getOrCreate("default") : metricRegistry;
        this.metricsConfiguration = metricsConfiguration;
    }

    public MetricReporter registerGraphite()
    {
        // set up "standard" graphite push for metrics
        InetSocketAddress metricsServerLocation =
            new InetSocketAddress( metricsConfiguration.getGraphiteEndpoint(), metricsConfiguration.getGraphitePort() );
        Graphite standardPusher = new Graphite(metricsServerLocation);
        GraphiteReporter reporter = GraphiteReporter.forRegistry(metricRegistry)
            .convertRatesTo(TimeUnit.MILLISECONDS)
            // N.B 1m is "somewhat" arbitrary - it controls the retention period of the metrics for comparision purposes
            .prefixedWith( metricsConfiguration.getGraphitePath() )
            .build(standardPusher);

        reporter.start(metricsConfiguration.getMetricsReportInterval(), TimeUnit.MILLISECONDS);
        reporters.add(reporter);
        return this;
    }

    public MetricReporter registerLogging()
    {
        Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
            .convertRatesTo(TimeUnit.MILLISECONDS)
            .build();

        reporter.start(metricsConfiguration.getMetricsReportInterval(), TimeUnit.MILLISECONDS);
        reporters.add(reporter);
        return this;
    }

    public MetricRegistry getMetricRegistry()
    {
        return metricRegistry;
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
