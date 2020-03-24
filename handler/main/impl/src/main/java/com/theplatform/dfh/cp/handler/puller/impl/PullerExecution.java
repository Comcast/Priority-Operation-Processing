package com.theplatform.dfh.cp.handler.puller.impl;

import com.theplatform.dfh.cp.handler.puller.impl.context.ExecutionContext;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullerExecution implements AliveCheck
{
    private ExecutionContext executionContext;

    public PullerExecution(PullerEntryPoint pullerEntryPoint)
    {
        Runnable executePuller = pullerEntryPoint::execute;
        Thread pullerThread = new Thread(executePuller);
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

    @Override
    public boolean isAlive()
    {
        return executionContext != null && executionContext.isThreadAlive();
    }
}