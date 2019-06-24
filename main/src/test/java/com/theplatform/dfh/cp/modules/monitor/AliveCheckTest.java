package com.theplatform.dfh.cp.modules.monitor;

import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheck;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckConfiguration;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckPoller;
import com.theplatform.dfh.cp.modules.monitor.alive.LogAliveCheckListener;
import com.theplatform.dfh.cp.modules.monitor.metric.LoggingMetricReporterFactory;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricAliveCheckListener;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

public class AliveCheckTest implements AliveCheck
{
    private Random random = new Random();

    @Override
    public boolean isAlive()
    {
        return random.nextBoolean();
    }
    @Test(enabled = false)
    public void testAlive() throws Exception
    {
        Properties serviceProperties = PropertyLoader.loadResource("../../../../../../service.properties");
        final int logMetricFrequency = 500;
        MetricReporter reporter = new MetricReporter(null);
        reporter.register(new LoggingMetricReporterFactory(logMetricFrequency));
        LogAliveCheckListener loggerAliveCheck = new LogAliveCheckListener();
        MetricAliveCheckListener metricsAliveCheck = new MetricAliveCheckListener(reporter);
        AliveCheckConfiguration aliveCheckConfiguration = new AliveCheckConfiguration(serviceProperties);
        AliveCheckPoller poller = new AliveCheckPoller(aliveCheckConfiguration, this, Arrays.asList(loggerAliveCheck, metricsAliveCheck));
        poller.start();
        long endTime = (aliveCheckConfiguration.getAliveCheckFrequencyMilliseconds() * 10);
        Thread.sleep(endTime);
    }
}
