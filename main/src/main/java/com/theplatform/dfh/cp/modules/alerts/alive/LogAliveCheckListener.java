package com.theplatform.dfh.cp.modules.alerts.alive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogAliveCheckListener implements AliveCheckListener
{
    private static final Logger logger = LoggerFactory.getLogger(LogAliveCheckListener.class);

    @Override
    public void processAliveCheck(boolean isAlive)
    {
         if(isAlive && logger.isDebugEnabled())
         {
             logger.debug("Received a successful alive check.");
         }
         else if(!isAlive)
         {
             logger.error("Received a failed alive check.");
         }
    }
}
