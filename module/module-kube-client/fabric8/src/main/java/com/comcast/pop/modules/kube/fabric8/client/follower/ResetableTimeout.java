package com.comcast.pop.modules.kube.fabric8.client.follower;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

/**
 *
 */
public class ResetableTimeout
{
    private static Logger logger = LoggerFactory.getLogger(ResetableTimeout.class);
    private final long maxWait;
    private long start = System.currentTimeMillis();
    private int inactivityCounter = 0;

    public ResetableTimeout(long maxWait)
    {
        this.maxWait = maxWait;
    }

    public ResetableTimeout()
    {
        this.maxWait = 60 * 1000 * 5;
    }

    public void timeout(String podName) throws TimeoutException
    {
        inactivityCounter++;
        long currentWaitTime;
        currentWaitTime = System.currentTimeMillis() - start;
        long ttl = maxWait - currentWaitTime;
        if(currentWaitTime > 1000)
        {
            logger.debug("Waited this long for pod [{}]: {}ms, will wait this much longer {}ms",
                podName,
                currentWaitTime,
                ttl);
        }
        if (ttl <= 0)
        {
            throw new TimeoutException("Waited too long!");
        }
    }

    public void reset()
    {
        logger.trace("Reseting wait.");
        start = System.currentTimeMillis();
        inactivityCounter = 0;
    }

    public int getInactivityCounter()
    {
        return inactivityCounter;
    }
}
