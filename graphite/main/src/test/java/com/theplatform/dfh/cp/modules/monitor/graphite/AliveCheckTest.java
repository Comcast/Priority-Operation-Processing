package com.theplatform.dfh.cp.modules.monitor.graphite;

import com.theplatform.dfh.cp.modules.monitor.PropertyLoader;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheck;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckConfiguration;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckPoller;
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
    public void testAlive()
    {
        Properties serviceProperties = PropertyLoader.loadResource("../../../../../../service.properties");
        GraphiteMetricReporterFactory graphiteFactory = new GraphiteMetricReporterFactory(serviceProperties);
        LoggingMetricReporterFactory loggingFactory = new LoggingMetricReporterFactory();
        MetricReporter report = new MetricReporter(null);
        report.register(graphiteFactory);
        report.register(loggingFactory);
        MetricAliveCheckListener metricsAliveCheck = new MetricAliveCheckListener(report);
        AliveCheckPoller poller = new AliveCheckPoller(new AliveCheckConfiguration(serviceProperties), this, Arrays.asList(metricsAliveCheck));
        poller.start();
    }
}
