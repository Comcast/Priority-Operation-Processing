package com.comcast.pop.modules.monitor.config;

import com.comcast.pop.modules.monitor.config.converter.StringArrayPropertyConverter;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigurationPropertiesTest
{
    @Test
    public void testNonKey()
    {
        Properties properties = new Properties();
        properties.put("my.property", "pop");
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
        ConfigKey<Boolean> testConfigKey = new ConfigKey<>("alert.enabled", true, Boolean.class);
        ConfigKeys configKeys = createConfigKeys(testConfigKey);

        Properties properties = new Properties();
        properties.put("my.property", "pop");
        properties.put(testConfigKey.getPropertyKey(), Boolean.FALSE.toString());

        ConfigurationProperties configurationProperties = ConfigurationProperties.from(properties, configKeys);
        Assert.assertNotNull(configurationProperties);
        Assert.assertNotNull(configurationProperties.getKeys());
        Assert.assertEquals(configurationProperties.getKeys().size(), 2);
        Assert.assertTrue(configurationProperties.getKeys().contains(testConfigKey));
        Assert.assertFalse(configurationProperties.get(testConfigKey));
    }
    @Test
    public void testWrongCasing()
    {
        ConfigKey<Boolean> testConfigKey = new ConfigKey<>("alert.enabled", true, Boolean.class);
        ConfigKeys configKeys = createConfigKeys(testConfigKey);

        Properties properties = new Properties();
        properties.put(testConfigKey.getPropertyKey().toUpperCase(), Boolean.FALSE.toString());

        ConfigurationProperties configurationProperties = ConfigurationProperties.from(properties, configKeys);
        Assert.assertNotNull(configurationProperties);
        Assert.assertNotNull(configurationProperties.getKeys());
        Assert.assertEquals(configurationProperties.getKeys().size(), 1);
        Assert.assertTrue(configurationProperties.getKeys().contains(testConfigKey));
        Assert.assertFalse(configurationProperties.get(testConfigKey));
    }
    @DataProvider
    public Object[][] delimitedPropertyValuePropertyProvider()
    {
        return new Object[][]
            {
                {"", new String[]{""}},
                {"test", new String[]{"test"}},
                {"test1;test2", new String[]{"test1", "test2"}}
            };
    }

    @Test(dataProvider = "delimitedPropertyValuePropertyProvider")
    public void testStringArrayProperty(String propertyValue, String[] expectedResult)
    {
        final String KEY = "my.property";
        ConfigKey<String[]> testConfigKey = new ConfigKey<>(KEY, null, String[].class, new StringArrayPropertyConverter(";"));
        ConfigKeys configKeys = createConfigKeys(testConfigKey);

        Properties properties = new Properties();
        properties.put("junk.property", "junk.value");
        properties.put(KEY, propertyValue);
        ConfigurationProperties configurationProperties = ConfigurationProperties.from(properties, configKeys);
        Assert.assertNotNull(configurationProperties);
        Assert.assertNotNull(configurationProperties.getKeys());
        // confirm all items in the properties are mapped
        Assert.assertEquals(configurationProperties.getKeys().size(), properties.size());
        Assert.assertTrue(configurationProperties.getKeys().contains(testConfigKey));
        Assert.assertEquals(configurationProperties.get(testConfigKey), expectedResult);
    }

    private ConfigKeys createConfigKeys(ConfigKey... configKeys)
    {
        return new ConfigKeys()
        {
            @Override
            public Set getKeys()
            {
                return Arrays.stream(configKeys).collect(Collectors.toSet());
            }
        };
    }
}
