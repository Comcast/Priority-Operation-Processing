package com.comcast.pop.handler.puller.impl;

import com.comcast.pop.handler.puller.impl.context.ExecutionContext;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheck;

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