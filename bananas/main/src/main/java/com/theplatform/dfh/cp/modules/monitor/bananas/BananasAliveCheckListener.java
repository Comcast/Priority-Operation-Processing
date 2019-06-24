package com.theplatform.dfh.cp.modules.monitor.bananas;

import com.comcast.cts.timeshifted.pump.bananas.BananasMessage;
import com.comcast.cts.timeshifted.pump.bananas.BananasMessageDecider;
import com.comcast.cts.timeshifted.pump.bananas.BananasMessageSender;
import com.comcast.cts.timeshifted.pump.status.AppState;
import com.comcast.cts.timeshifted.pump.status.StatusQueue;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckConfiguration;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Sends bananas alive check reports. See AliveCheckPoller
 */
public class BananasAliveCheckListener implements AliveCheckListener
{
    private static final Logger logger = LoggerFactory.getLogger(BananasAliveCheckListener.class);
    private BananasMessageDecider sender;
    private StatusQueue statusQueue;

    public BananasAliveCheckListener(Properties serviceProperties)
    {
        this(new AlertingConfiguration(serviceProperties), new AliveCheckConfiguration(serviceProperties), new BananasConfiguration(serviceProperties));
    }
    public BananasAliveCheckListener(AlertingConfiguration alertingConfiguration, AliveCheckConfiguration aliveCheckConfiguration, BananasConfiguration bananasConfiguration)
    {
        this.statusQueue = new StatusQueue(alertingConfiguration);
        BananasMessage.BananaMessageBuilder builder = new BananasMessage.BananaMessageBuilder();
        builder.withDescription(aliveCheckConfiguration.getAliveCheckAlertDescription());
        BananasMessage message = builder.withBananasConfigDefaults(bananasConfiguration).create();

        this.sender = new BananasMessageDecider(message, new BananasMessageSender(bananasConfiguration));
    }
    public void processAliveCheck(boolean isAlive)
    {
        try
        {
            AppState previousState = statusQueue.getCurrentOverallStatus();
            //do something
            if (isAlive)
            {
                statusQueue.addSuccessAndEvaluateAppState();
            }
            else
            {
                logger.error("isAlive = failed");
                statusQueue.addFailureAndUpdateAppState();
            }
            sender.sendAlert(previousState, statusQueue);
        }
        catch (Throwable e)
        {
            //don't crash our application if we can't send alerts.
            logger.error("Error occurred trying to get/send alerts via Bananas. Ignoring until next check.", e);
        }
    }
}
