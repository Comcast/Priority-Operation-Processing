package com.theplatform.dfh.cp.handler.base.progress.reporter.operation;

import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.base.progress.OperationProgressFactory;

import java.util.Collections;
import java.util.List;

/**
 * A reporter specific to operation progress. Uses a basic ProgressFactory internally for simple progress creates.
 */
public class OperationProgressReporterImpl implements OperationProgressReporter
{
    private OperationProgressConsumer operationProgressConsumer;
    private OperationProgressFactory operationProgressFactory;

    public OperationProgressReporterImpl(OperationProgressThread operationProgressConsumer, OperationProgressFactory operationProgressFactory)
    {
        this.operationProgressConsumer = operationProgressConsumer;
        this.operationProgressFactory = operationProgressFactory;
    }

    /**
     * Adds progress with the specified state and message
     * @param processingState The state to indicate
     * @param processingStateMessage The message to indicate
     */
    @Override
    public void addProgress(ProcessingState processingState, String processingStateMessage)
    {
        operationProgressConsumer.setOperationProgress(
            operationProgressFactory.create(processingState, processingStateMessage));
    }

    /**
     * Adds percentage progress (as executing ProcessingState)
     * @param processingStateMessage The message to indicate
     * @param percentComplete The percent complete to indicate
     */
    @Override
    public void addProgress(String processingStateMessage, Double percentComplete)
    {
        OperationProgress operationProgress = operationProgressFactory.create(
            ProcessingState.EXECUTING, processingStateMessage, percentComplete);
        operationProgressConsumer.setOperationProgress(operationProgress);
    }

    /**
     * Adds percentage progress (as executing ProcessingState)
     * @param percentComplete The percent complete to indicate
     */
    @Override
    public void addProgress(Double percentComplete)
    {
        addProgress(null, percentComplete);
    }

    /**
     * General method for adding progress. The object is directly passed to the progress consumer.
     * @param operationProgress The progress to report
     */
    @Override
    public void addOperationProgress(OperationProgress operationProgress)
    {
        operationProgressConsumer.setOperationProgress(operationProgress);
    }

    /**
     * General method for adding progress. The objects are directly passed to the progress consumer.
     * @param operationProgress The progress to report
     * @param result The result to pass through to the consumer
     */
    @Override
    public void addOperationProgress(OperationProgress operationProgress, Object result)
    {
        operationProgressConsumer.setOperationProgress(operationProgress, result);
    }

    /**
     * Adds a complete and succeeded progress
     * @param result The resulting object to pass back
     */
    @Override
    public void addSucceeded(Object result)
    {
        operationProgressConsumer.setOperationProgress(
            operationProgressFactory.create(ProcessingState.COMPLETE, CompleteStateMessage.SUCCEEDED.toString()),
            result
        );
    }

    /**
     * Adds a complete and failed progress with a diagnostic event
     * @param diagnosticEvent The diagnostic event to pass back
     */
    @Override
    public void addFailed(DiagnosticEvent diagnosticEvent)
    {
        addFailed(Collections.singletonList(diagnosticEvent), null);
    }

    /**
     * Adds a complete and failed progress with diagnostic events
     * @param diagnosticEvents The diagnostic events to pass back
     */
    @Override
    public void addFailed(List<DiagnosticEvent> diagnosticEvents)
    {
        addFailed(diagnosticEvents, null);
    }

    /**
     * Adds a complete and failed progress with diagnostic events
     * @param diagnosticEvents The diagnostic events to pass back
     * @param payload The payload object to pass back
     */
    @Override
    public void addFailed(List<DiagnosticEvent> diagnosticEvents, Object payload)
    {
        operationProgressConsumer.setOperationProgress(
            operationProgressFactory.createWithEvents(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), diagnosticEvents),
            payload
        );
    }

    public OperationProgressConsumer getOperationProgressConsumer()
    {
        return operationProgressConsumer;
    }

    public OperationProgressReporterImpl setOperationProgressConsumer(OperationProgressConsumer operationProgressConsumer)
    {
        this.operationProgressConsumer = operationProgressConsumer;
        return this;
    }

    public OperationProgressFactory getOperationProgressFactory()
    {
        return operationProgressFactory;
    }

    public OperationProgressReporterImpl setOperationProgressFactory(OperationProgressFactory operationProgressFactory)
    {
        this.operationProgressFactory = operationProgressFactory;
        return this;
    }
}
