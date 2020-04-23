package com.comcast.pop.modules.monitor.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertReporter
{
    private static final Logger logger = LoggerFactory.getLogger(AlertReporter.class);
    private static final int DEFAULT_ERROR_THRESHOLD = 2;
    private AlertLevel alertFailureLevel = AlertLevel.INFO;
    private AlertLevel alertPassedLevel = AlertLevel.CLEAR;
    private int alertCountThreshold;
    private int alertCount = 0;
    private boolean isAlerting = false;
    private AlertMessage alertMessage;
    private AlertSender alertSender;

    public AlertReporter(AlertMessage alertMessage, AlertSender sender)
    {
        this(alertMessage, sender, DEFAULT_ERROR_THRESHOLD);
    }

    public AlertReporter(AlertMessage alertMessage, AlertSender sender, int alertCountThreshold)
    {
        this.alertCountThreshold = alertCountThreshold <= 0 ? DEFAULT_ERROR_THRESHOLD : alertCountThreshold;
        this.alertMessage = alertMessage;
        this.alertSender = sender;
    }
    public void setAlertFailedLevel(String level)
    {
        if(level != null)
            setAlertFailedLevel(AlertLevel.valueOf(level));
    }
    public void setAlertPassedLevel(String level)
    {
        if(level != null)
            setAlertPassedLevel(AlertLevel.valueOf(level));
    }
    public void setAlertFailedLevel(AlertLevel alertLevel)
    {
        if(alertLevel == AlertLevel.UNKNOWN)
        {
            logger.warn("Alert failure level unknown. Defaulting to {}", AlertConfigKeys.LEVEL_FAILED.getDefaultValue());
        }
        this.alertFailureLevel = alertLevel;
    }
    public void setAlertPassedLevel(AlertLevel alertLevel)
    {
        if(alertLevel == AlertLevel.UNKNOWN)
        {
            logger.warn("Alert passed level unknown. Defaulting to {}", AlertConfigKeys.LEVEL_PASSED.getDefaultValue());
        }
        this.alertPassedLevel = alertLevel;
    }

    public int getAlertCount()
    {
        return alertCount;
    }
    public boolean isAlerting()
    {
        return isAlerting;
    }
    public void markFailed()
    {
        markFailed(alertFailureLevel);
    }
    public void markFailed(final AlertLevel alertLevel)
    {
        alertCount ++;

        evaluateAndReport(alertLevel);
    }
    public void markPassed()
    {
        markPassed(alertPassedLevel);
    }
    public void markPassed(final AlertLevel alertLevel)
    {
        if(alertCount > 0)
            alertCount --;

        evaluateAndReport(alertLevel);
    }

    public void evaluateAndReport(final AlertLevel alertLevel)
    {
        if (alertCount >= alertCountThreshold)
        {
            isAlerting = true;
            alertMessage.setLevel(alertLevel);
            alertSender.send(alertMessage);
            alertCount = 0;
        }
        else if(alertCount == 0 && isAlerting)
        {
            isAlerting = false;
            alertMessage.setLevel(alertLevel);
            alertSender.send(alertMessage);
        }
    }
}
