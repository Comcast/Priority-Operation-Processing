package com.theplatform.dfh.cp.modules;

import com.comcast.cts.timeshifted.pump.configuration.MetricsConfiguration;
import com.theplatform.dfh.cp.modules.alerts.alive.*;
import com.theplatform.dfh.cp.modules.alerts.metric.MetricAliveCheckListener;
import com.theplatform.dfh.cp.modules.alerts.metric.MetricReporter;
import com.theplatform.dfh.cp.modules.alerts.PropertyLoader;
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
    public void testAlive()
    {
        Properties serviceProperties = PropertyLoader.loadResource("../../../../../../service.properties");
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration(serviceProperties);
        MetricReporter reporter = new MetricReporter(metricsConfiguration, null).registerLogging().registerGraphite();
        BananasAliveCheckListener bananasAliveCheck = new BananasAliveCheckListener(serviceProperties);
        LogAliveCheckListener loggerAliveCheck = new LogAliveCheckListener();
        MetricAliveCheckListener metricsAliveCheck = new MetricAliveCheckListener(reporter);
        AliveCheckPoller poller = new AliveCheckPoller(new AlertingConfiguration(serviceProperties), this, Arrays.asList(bananasAliveCheck, loggerAliveCheck, metricsAliveCheck));
        poller.start();
    }
}
