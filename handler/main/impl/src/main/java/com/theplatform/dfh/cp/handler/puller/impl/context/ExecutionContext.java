package com.theplatform.dfh.cp.handler.puller.impl.context;

/**
 * User: kimberly.todd
 * Date: 8/29/18
 */
public class ExecutionContext
{
    private Thread pullerThread;

    public ExecutionContext(Thread pullerThread)
    {
        this.pullerThread = pullerThread;
    }

    public void startThread()
    {
        pullerThread.start();
    }

    public boolean isThreadAlive()
    {
        return pullerThread.isAlive();
    }
}