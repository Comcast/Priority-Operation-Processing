package com.theplatform.dfh.cp.api.progress;

/**
 * waiting: waiting to start (pods or dependencies pending)
 * executing: actively processing
 * complete: completed (failed or succeeded)
 */
public enum ProcessingState
{
    WAITING,
    EXECUTING,
    COMPLETE
}
