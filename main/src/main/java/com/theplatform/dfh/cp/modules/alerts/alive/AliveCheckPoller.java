package com.theplatform.dfh.cp.modules.alerts.alive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AliveCheckPoller
{
    private static final Logger logger = LoggerFactory.getLogger(AliveCheckPoller.class);
    private AlertingConfiguration alertConfiguration;
    private AliveCheck aliveCheck;
    private boolean shouldContinueProcessing = true;
    private List<AliveCheckListener> listeners = new ArrayList<>();

    public AliveCheckPoller(AlertingConfiguration alertConfiguration, AliveCheck aliveCheck, List<AliveCheckListener> listeners)
    {
        if(listeners == null)
            throw new RuntimeException("No listeners registered to respond to the alive check.");
        this.alertConfiguration = alertConfiguration;
        this.aliveCheck = aliveCheck;
        this.listeners.addAll(listeners);
    }

    public void start()
    {
        if(aliveCheck == null)
            throw new RuntimeException("Unable to start alive check. Make sure your alive check class is configured.");
        do
        {
            try
            {
                //do something
                for (AliveCheckListener listener : listeners)
                {
                    listener.processAliveCheck(aliveCheck.isAlive());
                }
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                for (AliveCheckListener listener : listeners)
                {
                    listener.processAliveCheck(false);
                }
            }

            try
            {
                Thread.sleep(alertConfiguration.getHealthCheckFrequencyMilliseconds());
            } catch (InterruptedException e)
            {
                logger.error("Interrupted while waiting for next check");
            }
        }
        while(shouldContinueProcessing);
    }

    public void stop()
    {
        this.shouldContinueProcessing = false;
    }

}
