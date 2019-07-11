package com.theplatform.dfh.cp.modules.monitor.graphite;

import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Properties;

public class GraphiteConfiguration
{
    private ConfigurationProperties configurationProperties = new ConfigurationProperties();

    public GraphiteConfiguration()
    {
    }
    public GraphiteConfiguration(Properties properties)
    {
        configurationProperties = ConfigurationProperties.from(properties, new GraphiteConfigKeys());
    }

    public String getGraphitePath()
    {
        return configurationProperties.get(GraphiteConfigKeys.PATH);
    }

    public String getGraphiteEndpoint()
    {
        return configurationProperties.get(GraphiteConfigKeys.ENDPOINT);
    }

    public int getGraphitePort()
    {
        return configurationProperties.get(GraphiteConfigKeys.PORT);
    }

    public int getMetricsReportInterval()
    {
        return configurationProperties.get(GraphiteConfigKeys.REPORT_FREQUENCY);
    }

    public boolean isEnabled()
    {
        return configurationProperties.get(GraphiteConfigKeys.ENABLED);
    }
}
