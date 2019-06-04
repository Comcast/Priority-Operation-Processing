package com.theplatform.dfh.cp.modules.monitor.graphite;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.Properties;

public class GraphiteConfiguration
{
    private final String graphitePathPropertyKey = "graphite.path";
    private final String graphiteEndpointPropertyKey = "graphite.endpoint";
    private final String graphitePortPropertyKey = "graphite.port";
    private final String metricsReportIntervalMillisecondsPropertyKey = "metric.report.interval.milliseconds";

    private volatile String graphitePath;
    private volatile String graphiteEndpoint;
    private volatile int graphitePort;
    private volatile int metricsReportIntervalMilliseconds;

    public GraphiteConfiguration(Properties properties)
    {
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
