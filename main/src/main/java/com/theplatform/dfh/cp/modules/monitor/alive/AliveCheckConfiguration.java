package com.theplatform.dfh.cp.modules.monitor.alive;

import java.util.Properties;

public class AliveCheckConfiguration
{
    private static final String aliveCheckAlertDescriptionPropertyKey = "alive.check.alert.description";
    private static final String descriptionPropertyKey = "alert.description";
    private static final String healthCheckFrequencyMillisecondsPropertyKey = "health.check.frequency.milliseconds";
    private String aliveCheckAlertDescription;
    private Integer healthCheckFrequencyMillieconds;

    public AliveCheckConfiguration(Properties properties)
    {
        initDefaultProperties(properties);
        if (properties == null)
            return;

        final String aliveDescProperty = properties.getProperty(aliveCheckAlertDescriptionPropertyKey);
        if (aliveDescProperty != null)
        {
            this.aliveCheckAlertDescription = aliveDescProperty;
        }
        else
        {
            final String descProperty = properties.getProperty(descriptionPropertyKey);
            this.aliveCheckAlertDescription = descProperty == null ? "Alive check" : descProperty;
        }
        healthCheckFrequencyMillieconds = new Integer( properties.getProperty(healthCheckFrequencyMillisecondsPropertyKey) );
    }

    public String getAliveCheckAlertDescription()
    {
        return aliveCheckAlertDescription;
    }
    public Integer getAliveCheckFrequencyMilliseconds()
    {
        return healthCheckFrequencyMillieconds;
    }
    private static Properties initDefaultProperties(Properties properties)
    {
        if(properties.getProperty(healthCheckFrequencyMillisecondsPropertyKey) == null)
            properties.setProperty(healthCheckFrequencyMillisecondsPropertyKey, "10000");
        return properties;
    }
}
