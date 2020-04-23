package com.comcast.pop.handler.executor.impl.progress.agenda;

import com.comcast.pop.api.progress.OperationProgress;

/**
 * Interface for retrieving an OperationProgress
 */
public interface OperationProgressProvider
{
    /**
     * Retrieves an OperationProgress
     * @return An operation progress or null if none is available
     */
    OperationProgress retrieveOperationProgress();
}
