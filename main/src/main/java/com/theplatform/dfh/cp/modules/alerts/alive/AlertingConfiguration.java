package com.theplatform.dfh.cp.modules.alerts.alive;

import java.util.Properties;

public class AlertingConfiguration extends com.comcast.cts.timeshifted.pump.configuration.AlertingConfiguration
{
    private static final String aliveCheckAlertDescriptionPropertyKey = "alive.check.alert.description";
    private static final String descriptionPropertyKey = "alert.description";
    private static final String sampleSizePropertyKey = "sample.size";
    private static final String healthCheckFrequencyMillisecondsPropertyKey = "health.check.frequency.milliseconds";
    private static final String alertThresholdPropertyKey = "alert.threshold";
    private static final String dataPropagationDelayMillisecondsPropertyKey = "kinesis.data.propagation.delay.milliseconds";
    private String aliveCheckAlertDescription;

    public AlertingConfiguration(Properties properties)
    {
        super(initDefaultProperties(properties));
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
    }

    public String getAliveCheckAlertDescription()
    {
        return aliveCheckAlertDescription;
    }

    private static Properties initDefaultProperties(Properties properties)
    {
        if(properties.getProperty(sampleSizePropertyKey) == null)
            properties.setProperty(sampleSizePropertyKey, "3");
        if(properties.getProperty(healthCheckFrequencyMillisecondsPropertyKey) == null)
            properties.setProperty(healthCheckFrequencyMillisecondsPropertyKey, "10000");
        if(properties.getProperty(alertThresholdPropertyKey) == null)
            properties.setProperty(alertThresholdPropertyKey, "2");
        if(properties.getProperty(dataPropagationDelayMillisecondsPropertyKey) == null)
            properties.setProperty(dataPropagationDelayMillisecondsPropertyKey, "90000");
        return properties;
    }
}
