package com.theplatform.dfh.cp.modules.monitor.graphite;

import com.theplatform.dfh.cp.modules.monitor.PropertyLoader;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheck;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckPoller;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;
import com.theplatform.dfh.cp.modules.monitor.metric.LoggingMetricReporterFactory;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricAliveCheckListener;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

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
