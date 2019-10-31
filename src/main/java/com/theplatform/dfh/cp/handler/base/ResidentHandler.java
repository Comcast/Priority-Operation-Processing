package com.theplatform.dfh.cp.handler.base;

import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.reporter.ProgressReporter;

/**
 * Represents a handler that is executed directly as a class (no external launch to execute)
 */
public interface ResidentHandler
{
    String execute(String payload, LaunchDataWrapper launchDataWrapper, ProgressReporter<OperationProgress> reporter);
}
