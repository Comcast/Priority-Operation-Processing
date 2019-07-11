package com.theplatform.dfh.cp.modules.monitor.alive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Threaded poller to poll call AliveCheck.isAlive() and let the listeners process.
 */
public class AliveCheckPoller implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(AliveCheckPoller.class);
    private Integer aliveCheckFrequency;
    private AliveCheck aliveCheck;
    private List<AliveCheckListener> listeners = new ArrayList<>();
    private ScheduledFuture future;

    public AliveCheckPoller(Integer aliveCheckFrequency, AliveCheck aliveCheck, List<AliveCheckListener> listeners)
    {
        if(listeners == null)
            throw new RuntimeException("No listeners registered to respond to the alive check.");
        this.aliveCheckFrequency = aliveCheckFrequency == null ? AliveCheckConfigKeys.CHECK_FREQUENCY.getDefaultValue() : aliveCheckFrequency;
        this.aliveCheck = aliveCheck;
        this.listeners.addAll(listeners);
    }

    public void start()
    {
        if(aliveCheck == null)
            throw new RuntimeException("Unable to start alive check. Make sure your alive check class is configured.");
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        future = executor.scheduleWithFixedDelay(this, 0, aliveCheckFrequency, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run()
    {
        try
        {
            //do something
            boolean isAlive = aliveCheck.isAlive();
            for (AliveCheckListener listener : listeners)
            {
                listener.processAliveCheck(isAlive);
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
    }

    public void stop()
    {
        future.cancel(false);
    }

}
