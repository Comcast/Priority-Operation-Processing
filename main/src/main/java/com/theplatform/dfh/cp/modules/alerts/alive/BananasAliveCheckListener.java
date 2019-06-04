package com.theplatform.dfh.cp.modules.alerts.alive;

import com.comcast.cts.timeshifted.pump.bananas.BananasMessage;
import com.comcast.cts.timeshifted.pump.bananas.BananasMessageDecider;
import com.comcast.cts.timeshifted.pump.bananas.BananasMessageSender;
import com.comcast.cts.timeshifted.pump.configuration.BananasConfiguration;
import com.comcast.cts.timeshifted.pump.status.AppState;
import com.comcast.cts.timeshifted.pump.status.StatusQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class BananasAliveCheckListener implements AliveCheckListener
{
    private static final Logger logger = LoggerFactory.getLogger(BananasAliveCheckListener.class);
    private BananasMessageDecider sender;
    private StatusQueue statusQueue;

    public BananasAliveCheckListener(Properties serviceProperties)
    {
        this(new AlertingConfiguration(serviceProperties), new BananasConfiguration(serviceProperties));
    }
    public BananasAliveCheckListener(AlertingConfiguration alertConfigruation, BananasConfiguration bananasConfiguration)
    {
        this.statusQueue = new StatusQueue(alertConfigruation);

        BananasMessage.BananaMessageBuilder builder = new BananasMessage.BananaMessageBuilder();
        builder.withDescription(alertConfigruation.getAliveCheckAlertDescription());
        BananasMessage message = builder.withBananasConfigDefaults(bananasConfiguration).create();

        this.sender = new BananasMessageDecider(message, new BananasMessageSender(bananasConfiguration));
    }
    public void processAliveCheck(boolean isAlive)
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
}
