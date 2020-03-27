package com.comcast.fission.reaper.objects.aws;

public abstract class BaseBatchedOperation
{
    /**
     * Delays for the specified milliseconds, if greater than 0.
     * @param delayMillis The milliseconds to wait
     * @return true on success, false if interrupted
     */
    protected boolean delay(long delayMillis)
    {
        if(delayMillis <= 0) return true;

        try
        {
            Thread.sleep(delayMillis);
            return true;
        }
        catch(InterruptedException e)
        {
            return false;
        }
    }
}
