package com.comcast.pop.scheduling.monitor.aws;


import com.comcast.pop.modules.monitor.graphite.GraphiteConfigKeys;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class MetricReporterFactoryTest
{
    @Test
    public void testRemap()
    {
        Properties properties = new Properties();
        properties.put("GRAPHITE_ENDPOINT", "XYZ");
        properties.put("GRAPHITE_PATH", "123");
        MetricReporterFactory.remap(new GraphiteConfigKeys().getKeys(), properties);
        Assert.assertEquals(properties.getProperty(GraphiteConfigKeys.ENDPOINT.getPropertyKey()), "XYZ");
        Assert.assertEquals(properties.getProperty(GraphiteConfigKeys.PATH.getPropertyKey()), "123");
    }
}
