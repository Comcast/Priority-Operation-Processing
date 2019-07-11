package com.theplatform.dfh.cp.modules.monitor.metric;

import com.codahale.metrics.Counter;
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
