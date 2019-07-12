package com.theplatform.dfh.cp.modules.monitor.bananas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.modules.monitor.bananas.config.BananasConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.bananas.message.BananasMessage;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringWriter;
import java.util.Properties;

public class BananasMessageTest
{
    @Test
    public void testBananasOnlyProperties() throws Exception
    {
        Properties serviceProperties = new Properties();
        generateBananasProperties(serviceProperties);
        Assert.assertNotNull(serviceProperties);
        ConfigurationProperties configurationProperties = ConfigurationProperties.from(serviceProperties, new BananasConfigKeys());
        Assert.assertNotNull(configurationProperties);
        BananasMessage bananasMessage = BananasMessage.fromConfigurationProperties(configurationProperties);
        Assert.assertNotNull(bananasMessage);
        Assert.assertEquals(bananasMessage.getHost(), serviceProperties.getProperty(BananasConfigKeys.HOST.getPropertyKey()));
        Assert.assertEquals(bananasMessage.getDescription(), serviceProperties.getProperty(BananasConfigKeys.DESCRIPTION.getPropertyKey()));
        Assert.assertEquals(bananasMessage.getService(), serviceProperties.getProperty(BananasConfigKeys.SERVICE.getPropertyKey()));
        Assert.assertEquals(bananasMessage.getTags(), new String[] { "zenoss","slack"});

        Assert.assertEquals(bananasMessage.getAttributes().get(BananasConfigKeys.SERVICE_CODE.getAttributeKey()), serviceProperties.getProperty(BananasConfigKeys.SERVICE_CODE.getPropertyKey()));

        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, bananasMessage);
        System.out.println(writer.toString());
    }


    private void generateBananasProperties(Properties properties)
    {
        properties.put(BananasConfigKeys.SERVICE.getPropertyKey(), "dfh");
        properties.put(BananasConfigKeys.SERVICE_CODE.getPropertyKey(), "dfh");
        properties.put(BananasConfigKeys.DESCRIPTION.getPropertyKey(), "dfh alert");
        properties.put(BananasConfigKeys.HOST.getPropertyKey(), "lab-main-t-aor-dfh-t01");
        properties.put(BananasConfigKeys.TAGS.getPropertyKey(), "zenoss,slack");
    }
}
