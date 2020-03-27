package com.cts.fission.scheduling.monitor.aws;

import com.codahale.metrics.MetricFilter;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigKey;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;
import com.theplatform.dfh.cp.modules.monitor.graphite.GraphiteConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.graphite.GraphiteMetricReporterFactory;
import com.theplatform.dfh.cp.modules.monitor.metric.LoggingConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.metric.LoggingMetricReporterFactory;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MetricReporterFactory
{
    private final Logger logger = LoggerFactory.getLogger(MetricReporterFactory.class);
    private MetricReporter reporter = null;

    public MetricReporterFactory()
    {
    }

    public MetricReporter createInstance(Map<String, String> environmentVariables)
    {
        if(reporter != null)
            reporter.close();

        reporter = new MetricReporter();
        Properties properties = new Properties();
        if(environmentVariables != null)
            properties.putAll(environmentVariables);
        //remap from AWS with _ to properties with . notation
        GraphiteConfigKeys graphiteConfigKeys = new GraphiteConfigKeys();
        LoggingConfigKeys loggingConfigKeys = new LoggingConfigKeys();
        remap(graphiteConfigKeys.getKeys(), properties);
        remap(loggingConfigKeys.getKeys(), properties);

        ConfigurationProperties configurationProperties = ConfigurationProperties.from(properties, graphiteConfigKeys, loggingConfigKeys);
        LoggingMetricReporterFactory loggingFactory = new LoggingMetricReporterFactory(configurationProperties, MetricFilter.ALL);
        logger.info("Registering logger metric reporter enabled: {}", configurationProperties.get(LoggingConfigKeys.ENABLED));
        reporter.register(loggingFactory);

        if(!configurationProperties.get(GraphiteConfigKeys.ENABLED) || configurationProperties.get(GraphiteConfigKeys.ENDPOINT) == null)
        {
            logger.error("Graphite configuration not found. Ignoring Graphite reporting.");
        }
        else
        {
            logger.debug("Graphite configuration found. Using {} with {}",
                configurationProperties.get(GraphiteConfigKeys.ENDPOINT) +":" +configurationProperties.get(GraphiteConfigKeys.PORT),
                configurationProperties.get(GraphiteConfigKeys.PATH));
            GraphiteMetricReporterFactory graphiteFactory = new GraphiteMetricReporterFactory(configurationProperties);
            reporter.register(graphiteFactory);
        }

        return reporter;
    }

    protected static void remap(Set<ConfigKey> keys, Properties properties)
    {
        for(ConfigKey key : keys)
        {
            String remappedKey = key.getPropertyKey().toUpperCase().replace('.', '_');
            String value = properties.getProperty(remappedKey);
            if(value != null)
                properties.put(key.getPropertyKey(), value);
        }
    }
}
