package com.theplatform.dfh.cp.handler.sample.impl.action;

import com.theplatform.dfh.cp.handler.reporter.api.Reporter;
import com.theplatform.dfh.cp.handler.sample.api.ActionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseAction
{
    private static Logger logger = LoggerFactory.getLogger(BaseAction.class);

    public void performAction(Reporter reporter, ActionParameters actionParameters)
    {
        Long sleepMilliseconds = actionParameters.getSleepMilliseconds();
        if(sleepMilliseconds != null)
        {
            try
            {
                logger.info("Performing requested sleep: {}ms", sleepMilliseconds);
                Thread.sleep(sleepMilliseconds);
            }
            catch (InterruptedException e)
            {
                // fine, do not care
            }
        }
        perform(reporter, actionParameters);
    }
    protected abstract void perform(Reporter reporter, ActionParameters actionParameters);
}
