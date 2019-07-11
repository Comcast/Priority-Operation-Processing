package com.theplatform.dfh.cp.modules.monitor.config;

import com.theplatform.dfh.cp.modules.monitor.alert.AlertConfigKeys;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class ConfigurationPropertiesTest
{
    @Test
    public void testNonKey()
    {
        Properties properties = new Properties();
        properties.put("my.property", "fission");
        ConfigurationProperties configurationProperties = ConfigurationProperties.from(properties, null);
        Assert.assertNotNull(configurationProperties);
        Assert.assertNotNull(configurationProperties.getKeys());
        Assert.assertEquals(configurationProperties.getKeys().size(), 1);
        ConfigKey key = configurationProperties.getKeys().iterator().next();
        Assert.assertEquals(key.getPropertyKey(), "my.property");
    }
    @Test
    public void testOneKeyAndOneNon()
    {
        Properties properties = new Properties();
        properties.put("my.property", "fission");
        properties.put(AlertConfigKeys.LEVEL_FAILED.getPropertyKey(), false);
        ConfigurationProperties configurationProperties = ConfigurationProperties.from(properties, new AlertConfigKeys());
        Assert.assertNotNull(configurationProperties);
        Assert.assertNotNull(configurationProperties.getKeys());
        Assert.assertEquals(configurationProperties.getKeys().size(), 2);
        Assert.assertTrue(configurationProperties.getKeys().contains(AlertConfigKeys.LEVEL_FAILED));
    }
}
