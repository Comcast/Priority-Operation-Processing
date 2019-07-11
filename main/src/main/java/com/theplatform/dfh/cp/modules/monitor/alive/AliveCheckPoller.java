package com.theplatform.dfh.cp.modules.monitor.alive;

import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
    private Boolean isEnabled;
    private AliveCheck aliveCheck;
    private List<AliveCheckListener> listeners = new ArrayList<>();
    private ScheduledFuture future;

    public AliveCheckPoller(Properties properties, AliveCheck aliveCheck, List<AliveCheckListener> listeners)
    {
        this(ConfigurationProperties.from(properties, new AliveCheckConfigKeys()), aliveCheck, listeners);
    }

    public AliveCheckPoller(ConfigurationProperties configurationProperties, AliveCheck aliveCheck, List<AliveCheckListener> listeners)
    {
        if(listeners == null)
            throw new IllegalArgumentException("No listeners registered to respond to the alive check.");
        if(aliveCheck == null)
            throw new IllegalArgumentException("Unable to start alive check. Make sure your alive check class is configured.");

        isEnabled = configurationProperties.get(AliveCheckConfigKeys.ENABLED) != null ? configurationProperties.get(AliveCheckConfigKeys.ENABLED) : Boolean.FALSE;

        Integer frequency = configurationProperties.get(AliveCheckConfigKeys.CHECK_FREQUENCY);
        this.aliveCheckFrequency = frequency == null ? AliveCheckConfigKeys.CHECK_FREQUENCY.getDefaultValue() : frequency;
        this.aliveCheck = aliveCheck;
        this.listeners.addAll(listeners);
    }

    public void start()
    {
        if(!isEnabled)
        {
            logger.error("Alive check monitoring is disabled.");
            return;
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        future = executor.scheduleWithFixedDelay(this, 0, aliveCheckFrequency, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run()
    {
        if(!isEnabled)
        {
            logger.error("Alive check monitoring is disabled.");
            return;
        }
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
        if(future != null)
            future.cancel(false);
    }

}
