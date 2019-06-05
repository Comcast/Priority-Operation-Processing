package com.theplatform.dfh.cp.modules.monitor.graphite;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Properties;

public class GraphiteConfiguration
{
    public static final String isEnabledPropertyKey = "graphite.enabled";
    public static final String graphitePathPropertyKey = "graphite.path";
    public static final String graphiteEndpointPropertyKey = "graphite.endpoint";
    public static final String graphitePortPropertyKey = "graphite.port";
    public static final String metricsReportIntervalMillisecondsPropertyKey = "metric.report.interval.milliseconds";

    private volatile boolean isEnabled = false;
    private volatile String graphitePath;
    private volatile String graphiteEndpoint;
    private volatile int graphitePort;
    private volatile int metricsReportIntervalMilliseconds = 300000;
    public GraphiteConfiguration()
    {
    }
    public GraphiteConfiguration(Properties properties)
    {
        isEnabled = BooleanUtils.toBoolean(properties.getProperty(isEnabledPropertyKey));
        graphitePath = properties.getProperty(graphitePathPropertyKey);
        graphiteEndpoint = properties.getProperty(graphiteEndpointPropertyKey);
        graphitePort = NumberUtils.toInt( properties.getProperty(graphitePortPropertyKey) );
        metricsReportIntervalMilliseconds = NumberUtils.toInt( properties.getProperty(metricsReportIntervalMillisecondsPropertyKey) );
    }

    public String getGraphitePath()
    {
        return graphitePath;
    }

    public String getGraphiteEndpoint()
    {
        return graphiteEndpoint;
    }

    public int getGraphitePort()
    {
        return graphitePort;
    }

    public int getMetricsReportInterval()
    {
        return metricsReportIntervalMilliseconds;
    }

    public int getMetricsReportIntervalMilliseconds()
    {
        return metricsReportIntervalMilliseconds;
    }

    public boolean isEnabled()
    {
        return this.isEnabled;
    }
    public void setEnabled(boolean enabled)
    {
        isEnabled = enabled;
    }

    public void setGraphitePath(String graphitePath)
    {
        this.graphitePath = graphitePath;
    }

    public void setGraphiteEndpoint(String graphiteEndpoint)
    {
        this.graphiteEndpoint = graphiteEndpoint;
    }

    public void setGraphitePort(int graphitePort)
    {
        this.graphitePort = graphitePort;
    }

    public void setMetricsReportIntervalMilliseconds(int metricsReportIntervalMilliseconds)
    {
        this.metricsReportIntervalMilliseconds = metricsReportIntervalMilliseconds;
    }
}
