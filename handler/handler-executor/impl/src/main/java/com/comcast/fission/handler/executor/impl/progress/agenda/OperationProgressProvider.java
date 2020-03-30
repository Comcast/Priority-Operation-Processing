package com.comcast.fission.handler.executor.impl.progress.agenda;

import com.theplatform.dfh.cp.api.progress.OperationProgress;

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
