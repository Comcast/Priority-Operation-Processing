package com.theplatform.dfh.cp.handler.puller.impl;

import com.theplatform.dfh.cp.handler.puller.impl.context.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullerExecution
{
    private static Logger logger = LoggerFactory.getLogger(PullerExecution.class);

    private ExecutionContext executionContext;

    public PullerExecution(PullerEntryPoint pullerEntryPoint)
    {
        int pullWait = pullerEntryPoint.getPullerConfig().getPullWait();

        Runnable executePuller = () -> {
            for (;;)
            {
                // todo unit test: loops a couple times and then aborts
                logger.info("Executing Puller");
                pullerEntryPoint.execute();
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