package com.theplatform.dfh.cp.modules.monitor;

import com.codahale.metrics.Counter;
import com.theplatform.dfh.cp.modules.monitor.metric.LoggingMetricReporterFactory;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricLabel;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MetricReporterTest
{
    private MetricReporter reporter = new MetricReporter();
    private LoggingMetricReporterFactory loggingMetricReporterFactory = new LoggingMetricReporterFactory();

    @BeforeClass
    public void beforeClass()
    {
        loggingMetricReporterFactory.register(reporter.getMetricRegistry());
        reporter.register(loggingMetricReporterFactory);
    }
    @Test
    public void testFailedCounter()
    {
        Counter counter = reporter.getCounter(MetricLabel.failed);
        Assert.assertNotNull(counter);
        Assert.assertEquals(counter.getCount(), 0);
        counter.inc();
        Assert.assertEquals(counter.getCount(), 1);
        reporter.report();

        counter.inc();
        Assert.assertEquals(counter.getCount(), 2);
        reporter.report();
    }
}
