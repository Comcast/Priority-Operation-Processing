package com.theplatform.dfh.cp.modules.monitor.alive;

import org.apache.commons.lang3.BooleanUtils;

import java.util.Properties;

public class AliveCheckConfiguration
{
    public static final String isEnabledPropertyKey = "alive.check.enabled";
    private static final String aliveCheckAlertDescriptionPropertyKey = "alive.check.alert.description";
    private static final String descriptionPropertyKey = "alert.description";
    private static final String healthCheckFrequencyMillisecondsPropertyKey = "health.check.frequency.milliseconds";
    private String aliveCheckAlertDescription;
    private Integer healthCheckFrequencyMillieconds;
    private volatile boolean isEnabled = false;

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
        isEnabled = BooleanUtils.toBoolean(properties.getProperty(isEnabledPropertyKey));
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

    public boolean isEnabled()
    {
        return this.isEnabled;
    }
    public void setEnabled(boolean enabled)
    {
        isEnabled = enabled;
    }
}
