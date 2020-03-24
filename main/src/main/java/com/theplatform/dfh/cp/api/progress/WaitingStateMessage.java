package com.theplatform.dfh.cp.api.progress;

/**
 * Messages when in the waiting state (likely will only ever have 1)
 */
public enum WaitingStateMessage
{
    PENDING("pending");

    private final String name;

    WaitingStateMessage(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
