package com.theplatform.dfh.cp.api.progress;

/**
 * Initialize
 * State in which the job is submitted and data is gathered via analyze and Accelerate logic
 * Initialize.queued : Queued for initialization
 * Initialize.executing : Executing analyze and accelerate logic
 * Initialize.complete : Waiting for run queue
 * Run
 * State in which the job is running it's operations
 * Run.queued : Queued for run execution
 * Run.executing : Executing operations (Encodings, packagings...)
 * Run.complete : TransformJob is complete
 */
public enum JobStatus
{
    INITIALIZE_QUEUED("initialize.queued"),
    INITIALIZE_EXECUTING("initialize.executing"),
    INITIALIZE_COMPLETE("initialize.complete"),
    RUN_QUEUED("run.queued"),
    RUN_EXECUTING("run.executing"),
    RUN_COMPLETE("run.complete");

    private final String name;

    private JobStatus(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
