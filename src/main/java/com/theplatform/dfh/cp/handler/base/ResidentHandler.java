package com.theplatform.dfh.cp.handler.base;

import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.ProgressReporter;

/**
 * Represents a handler that is executed directly as a class (no external launch to execute)
 */
public interface ResidentHandler
{
    String execute(String payload, LaunchDataWrapper launchDataWrapper, ProgressReporter reporter);
}
