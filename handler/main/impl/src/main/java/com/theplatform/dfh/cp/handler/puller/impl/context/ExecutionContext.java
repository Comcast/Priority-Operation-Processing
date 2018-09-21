package com.theplatform.dfh.cp.handler.puller.impl.context;

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

    @Deprecated
    public void stopThread()
    {
        pullerThread.stop();
    }
}