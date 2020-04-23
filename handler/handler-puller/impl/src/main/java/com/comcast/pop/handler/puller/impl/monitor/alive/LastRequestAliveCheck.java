package com.comcast.pop.handler.puller.impl.monitor.alive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic alive checker related to the last time a request was made.
 */
public class LastRequestAliveCheck implements AliveCheck
{
    private static Logger logger = LoggerFactory.getLogger(LastRequestAliveCheck.class);
    private static final String NOT_ALIVE_MESSAGE = "Too long since last agenda request.";
    public static String LAST_REQUEST_THRESHOLD_PROPERTY = "monitor.alive.lastRequestMilliseconds";

    private Long notAliveThresholdMilliseconds = 30000L;
    private Long lastRequestMilliseconds = null;

    @Override
    public boolean isAlive()
    {
        if(lastRequestMilliseconds != null)
        {
            long currentMilliseconds = System.currentTimeMillis();
            long difference = (currentMilliseconds - lastRequestMilliseconds);
            boolean result = difference < notAliveThresholdMilliseconds;
            if(!result)
                logger.warn(String.format("Last request sent: %1$s ms ago. Threshold: %2$s", difference, notAliveThresholdMilliseconds));
            return result;
        }
        return true;
    }

    @Override
    public String getNotAliveString()
    {
        return NOT_ALIVE_MESSAGE;
    }

    public void setNotAliveThresholdMilliseconds(Long notAliveThresholdMilliseconds)
    {
        this.notAliveThresholdMilliseconds = notAliveThresholdMilliseconds;
    }

    public void updateLastRequestDate()
    {
        lastRequestMilliseconds = System.currentTimeMillis();
    }
}
