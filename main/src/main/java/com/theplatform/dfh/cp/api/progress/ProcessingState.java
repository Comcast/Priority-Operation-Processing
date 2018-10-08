package com.theplatform.dfh.cp.api.progress;

/**
 * waiting: waiting to start (pods or dependencies pending)
 * executing: actively processing
 * complete: completed (failed or succeeded)
 */
public enum ProcessingState
{
    WAITING("waiting"),
    EXECUTING("executing"),
    COMPLETE("complete");


    private final String name;

    private ProcessingState(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
