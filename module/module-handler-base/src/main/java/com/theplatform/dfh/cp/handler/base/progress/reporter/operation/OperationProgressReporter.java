package com.theplatform.dfh.cp.handler.base.progress.reporter.operation;

import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;

import java.util.List;

/**
 * A reporter specific to operation progress. Uses a basic ProgressFactory internally for simple progress creates.
 */
public interface OperationProgressReporter
{
    void addProgress(ProcessingState processingState, String processingStateMessage);
    void addProgress(String processingStateMessage, Double percentComplete);
    void addProgress(Double percentComplete);
    void addOperationProgress(OperationProgress operationProgress);
    void addOperationProgress(OperationProgress operationProgress, Object result);
    void addSucceeded(Object result);
    void addFailed(DiagnosticEvent diagnosticEvent);
    void addFailed(List<DiagnosticEvent> diagnosticEvents);
    void addFailed(List<DiagnosticEvent> diagnosticEvents, Object payload);
}
