package com.theplatform.dfh.cp.modules.monitor.graphite;

import com.codahale.metrics.*;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class GraphiteReporterWrapper extends ScheduledReporter
{
    private static final Logger logger = LoggerFactory.getLogger(GraphiteReporterWrapper.class);
    private GraphiteReporter reporter;
    private ConfigurationProperties configurationProperties;

    public GraphiteReporterWrapper(ConfigurationProperties configurationProperties, MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit,
        TimeUnit durationUnit, GraphiteReporter reporter)
    {
        super(registry, name, filter, rateUnit, durationUnit);
        this.configurationProperties = configurationProperties;
        this.reporter = reporter;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters,
        SortedMap<String, Timer> timers)
    {
        if (configurationProperties != null && configurationProperties.get(GraphiteConfigKeys.ENABLED))
        {
            try
            {
                reporter.report(gauges, counters, histograms, meters, timers);
            }
            catch (Throwable e)
            {
                //don't crash our application if we can't send alerts.
                logger.error("Error occurred trying to get/send metrics via Graphite. Ignoring until next check.", e);
            }
        }
    }
}
