package com.comcast.pop.api.progress;

/**
 * succeeded: the process succeeded
 * failed: the process failed
 */
public enum CompleteStateMessage
{
    SUCCEEDED("succeeded"),
    FAILED("failed");

    private final String name;

    private CompleteStateMessage(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
