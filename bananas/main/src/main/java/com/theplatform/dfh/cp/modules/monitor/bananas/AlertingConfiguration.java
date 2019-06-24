package com.theplatform.dfh.cp.modules.monitor.bananas;

import org.apache.commons.lang.StringUtils;

import java.util.Properties;

/**
 * This class is a wrapper around AlertingConfiguration since that class doesn't do null checks or invalid params :(
 */
public class AlertingConfiguration extends com.comcast.cts.timeshifted.pump.configuration.AlertingConfiguration
{
    private static final String sampleSizePropertyKey = "sample.size";
    private static final String healthCheckFrequencyMillisecondsPropertyKey = "health.check.frequency.milliseconds";
    private static final String alertThresholdPropertyKey = "alert.threshold";
    private static final String dataPropagationDelayMillisecondsPropertyKey = "kinesis.data.propagation.delay.milliseconds";

    public AlertingConfiguration(Properties properties)
    {
        super(setDefaults(properties));
    }

    private static Properties setDefaults(Properties properties)
    {
        if(StringUtils.isEmpty(properties.getProperty(sampleSizePropertyKey)))
            properties.setProperty(sampleSizePropertyKey, "3");

        if(StringUtils.isEmpty(properties.getProperty(healthCheckFrequencyMillisecondsPropertyKey)))
            properties.setProperty(healthCheckFrequencyMillisecondsPropertyKey, "10000");

        if(StringUtils.isEmpty(properties.getProperty(alertThresholdPropertyKey)))
            properties.setProperty(alertThresholdPropertyKey, "2");

        if(StringUtils.isEmpty(properties.getProperty(dataPropagationDelayMillisecondsPropertyKey)))
            properties.setProperty(dataPropagationDelayMillisecondsPropertyKey, "20000");
        return properties;

    }
}
