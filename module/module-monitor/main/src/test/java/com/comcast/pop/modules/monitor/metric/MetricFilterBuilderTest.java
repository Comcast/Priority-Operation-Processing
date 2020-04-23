package com.comcast.pop.modules.monitor.metric;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.Timer;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MetricFilterBuilderTest
{
    @Test
    public void testFilterTimer()
    {
        MetricFilter filter = new MetricFilterBuilder().filterTimer().build();
        Assert.assertFalse(filter.matches(null, new Timer()));
        Assert.assertTrue(filter.matches(null, new Meter()));
    }
    @Test
    public void testFilterCount()
    {
        MetricFilter filter = new MetricFilterBuilder().filterCountZero().build();
        Counter counter = new Counter();
        counter.inc();
        Assert.assertTrue(filter.matches(null, counter));
        counter.dec();
        Assert.assertFalse(filter.matches(null, counter));
    }
}
