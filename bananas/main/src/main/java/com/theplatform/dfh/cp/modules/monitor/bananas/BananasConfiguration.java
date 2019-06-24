package com.theplatform.dfh.cp.modules.monitor.bananas;

import org.apache.commons.lang.StringUtils;

import java.util.Properties;

/**
 * This class is a wrapper around BananasConfiguration since that class doesn't do null checks or invalid params :(
 */
public class BananasConfiguration extends com.comcast.cts.timeshifted.pump.configuration.BananasConfiguration
{
    private static final String bananasSenderTimeoutMillisPropertyKey = "bananas.sender.timeout.milliseconds";
    private static final String bananasSenderRetryMillisPropertyKey = "bananas.sender.milliseconds.between.retries";
    private static final String bananaTagsPropertyKey = "banana.tags";

    public BananasConfiguration(Properties properties)
    {
        super(setDefaults(properties));
    }
    private static Properties setDefaults(Properties properties)
    {
        if(StringUtils.isEmpty(properties.getProperty(bananaTagsPropertyKey)))
            properties.setProperty(bananaTagsPropertyKey, ",");

        if(StringUtils.isEmpty(properties.getProperty(bananasSenderTimeoutMillisPropertyKey)))
            properties.setProperty(bananasSenderTimeoutMillisPropertyKey, "1000");

        if(StringUtils.isEmpty(properties.getProperty(bananasSenderRetryMillisPropertyKey)))
            properties.setProperty(bananasSenderRetryMillisPropertyKey, "200");

        return properties;
    }
}
