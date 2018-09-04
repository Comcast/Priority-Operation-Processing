package com.theplatform.dfh.cp.handler.puller.impl;

import com.theplatform.dfh.cp.handler.puller.impl.context.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: kimberly.todd
 * Date: 8/29/18
 */
public class PullerExecution
{
    private static Logger logger = LoggerFactory.getLogger(PullerExecution.class);

    private ExecutionContext executionContext;

    public PullerExecution(PullerEntryPoint pullerEntryPoint)
    {
        Runnable executePuller = () -> {
            for (;;)
            {
                try
                {
                    logger.info("Executing Puller");
                    pullerEntryPoint.execute();
                    Thread.sleep(20 * 1000);
                }
                catch (InterruptedException e)
                {
                    logger.error("", e);
                }
            }
        };
        Thread pullerThread =new Thread(executePuller);

        this.executionContext = new ExecutionContext(pullerThread);
    }

    public void start()
    {
        executionContext.startThread();
    }

    public ExecutionContext getExecutionContext()
    {
        return executionContext;
    }
}
