package com.comcast.pop.handler.kubernetes.support.monitor;

import com.codahale.metrics.MetricFilter;
import com.comast.pop.handler.base.field.retriever.properties.PropertyRetriever;
import com.comcast.pop.modules.monitor.graphite.GraphiteMetricReporterFactory;
import com.comcast.pop.modules.monitor.metric.LoggingMetricReporterFactory;
import com.comcast.pop.modules.monitor.metric.MetricReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricReporterFactory
{
    private static Logger logger = LoggerFactory.getLogger(MetricReporterFactory.class);

    public static MetricReporter getInstance(PropertyRetriever propertyRetriever)
    {
        return getInstance(propertyRetriever, MetricFilter.ALL);
    }
    public static MetricReporter getInstance(PropertyRetriever propertyRetriever, MetricFilter metricFilter)
    {
        MetricReporter metricReporter = new MetricReporter();
        if (propertyRetriever != null && propertyRetriever.getPropertyProvider() != null)
        {
            try
            {
                //register a logging reporter
                metricReporter.register(new LoggingMetricReporterFactory(propertyRetriever.getPropertyProvider().getProperties(), metricFilter));
                //register a graphite reporter if configured
                GraphiteMetricReporterFactory reporterFactory = new GraphiteMetricReporterFactory(propertyRetriever.getPropertyProvider().getProperties());
                metricReporter.register(reporterFactory);

                return metricReporter;
            }
            catch(Throwable e)
            {
                //don't crash our application if we can't send alerts.
                logger.error("Error occurred trying to initialize metrics via Graphite. Ignoring until next check.", e);
            }
        }
        return metricReporter;
    }
}
