package com.theplatform.dfh.cp.modules.monitor.bananas;

import com.theplatform.dfh.cp.modules.monitor.alert.AlertConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.alert.AlertException;
import com.theplatform.dfh.cp.modules.monitor.alert.AlertLevel;
import com.theplatform.dfh.cp.modules.monitor.alert.AlertReporter;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckListener;
import com.theplatform.dfh.cp.modules.monitor.bananas.config.BananasPropertiesFactory;
import com.theplatform.dfh.cp.modules.monitor.bananas.message.BananasMessage;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Sends bananas alive check reports. See AliveCheckPoller
 */
public class BananasAliveCheckListener implements AliveCheckListener
{
    private static final Logger logger = LoggerFactory.getLogger(BananasAliveCheckListener.class);
    private AlertReporter alertReporter;
    private BananasMessage message;
    private Boolean isEnabled;

    public BananasAliveCheckListener(Properties serviceProperties)
    {
        this(BananasPropertiesFactory.from(serviceProperties));
    }
    public BananasAliveCheckListener(ConfigurationProperties configurationProperties)
    {
        if(configurationProperties == null)
            throw new AlertException("Unable to configure bananas due to missing configuration.");
        isEnabled = configurationProperties.get(AlertConfigKeys.ENABLED) != null ? configurationProperties.get(AlertConfigKeys.ENABLED) : Boolean.FALSE;
        if(!isEnabled)
        {
            logger.error("Alert monitoring is disabled.");
            return;
        }
        message = BananasMessage.fromConfigurationProperties(configurationProperties);

        final String host = configurationProperties.get(AlertConfigKeys.HOST);
        final Integer retryTimeout = configurationProperties.get(AlertConfigKeys.RETRY_TIMEOUT);
        final Integer retryCount = configurationProperties.get(AlertConfigKeys.RETRY_COUNT);
        final String failedLevel = configurationProperties.get(AlertConfigKeys.LEVEL_FAILED);
        final String passedLevel = configurationProperties.get(AlertConfigKeys.LEVEL_PASSED);
        BananasSender sender = new BananasSender(host, retryTimeout, retryCount);
        
        alertReporter = new AlertReporter(message, sender);
        alertReporter.setAlertFailedLevel(failedLevel);
        alertReporter.setAlertPassedLevel(passedLevel);
    }

    public void processAliveCheck(boolean isAlive)
    {
        if(!isEnabled)
        {
            logger.error("Alert monitoring is disabled.");
            return;
        }
        try
        {
            //do something
            if (isAlive)
            {
                alertReporter.markPassed();
            }
            else
            {
                logger.error("isAlive = failed");
                alertReporter.markFailed();
            }
        }
        catch (Throwable e)
        {
            //don't crash our application if we can't send alerts.
            logger.error("Error occurred trying to get/send alerts via Bananas. Ignoring until next check.", e);
        }
    }
}
