package com.comcast.pop.modules.monitor.graphite;

import com.comcast.pop.modules.monitor.PropertyLoader;
import com.comcast.pop.modules.monitor.alive.AliveCheck;
import com.comcast.pop.modules.monitor.alive.AliveCheckConfigKeys;
import com.comcast.pop.modules.monitor.alive.AliveCheckPoller;
import com.comcast.pop.modules.monitor.config.ConfigurationProperties;
import com.comcast.pop.modules.monitor.metric.LoggingMetricReporterFactory;
import com.comcast.pop.modules.monitor.metric.MetricAliveCheckListener;
import com.comcast.pop.modules.monitor.metric.MetricReporter;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Properties;

public class AliveCheckTest implements AliveCheck
{
    private Boolean[] isAliveState = {  true, false, false, false, true };
    private boolean keepChecking = true;
    private int stateIndex = 0;
    private AliveCheckPoller poller;

    @Override
    public boolean isAlive()
    {
        if(stateIndex == isAliveState.length - 1)
        {
            poller.stop();
            keepChecking = false;
        }
        return isAliveState[stateIndex ++];
    }
    @Test(enabled = false)
    public void testAlive()
    {
        Properties serviceProperties = PropertyLoader.loadResource("../../../../../../service.properties");
        GraphiteMetricReporterFactory graphiteFactory = new GraphiteMetricReporterFactory(serviceProperties);
        LoggingMetricReporterFactory loggingFactory = new LoggingMetricReporterFactory();
        MetricReporter report = new MetricReporter(null);
        report.register(graphiteFactory);
        report.register(loggingFactory);
        MetricAliveCheckListener metricsAliveCheck = new MetricAliveCheckListener(report);
        ConfigurationProperties configurationProperties = ConfigurationProperties.from(serviceProperties, new AliveCheckConfigKeys());
        poller = new AliveCheckPoller(configurationProperties, this, Arrays.asList(metricsAliveCheck));
        poller.start();

        while(keepChecking)
        {

        }
    }
}
