package com.theplatform.dfh.cp.handler.executor.impl.progress.agenda;

import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.ProcessingState;

import java.util.Collections;
import java.util.List;

/**
 * A reporter specific to Agenda progress
 */
public class AgendaProgressReporter
{
    private AgendaProgressConsumer agendaProgressConsumer;
    private AgendaProgressFactory agendaProgressFactory;

    public AgendaProgressReporter(AgendaProgressConsumer agendaProgressConsumer, AgendaProgressFactory agendaProgressFactory)
    {
        this.agendaProgressConsumer = agendaProgressConsumer;
        this.agendaProgressFactory = agendaProgressFactory;
    }

    /**
     * Sets the basic state settings on the Agenda
     * @param processingState the state to set
     * @param processingStateMessage the state message to set
     */
    public void addProgress(ProcessingState processingState, String processingStateMessage)
    {
        agendaProgressConsumer.setAgendaProgress(
            agendaProgressFactory.createAgendaProgress(processingState, processingStateMessage)
        );
    }

    /**
     * Reports success
     */
    public void addSucceeded()
    {
        agendaProgressConsumer.setAgendaProgress(
            agendaProgressFactory.createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.SUCCEEDED.toString())
        );
    }

    /**
     * Adds a complete and failed progress
     */
    public void addFailed()
    {
        agendaProgressConsumer.setAgendaProgress(
            agendaProgressFactory.createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString())
        );
    }

    /**
     * Adds a complete and failed progress with a diagnostic event
     * @param diagnosticEvent The diagnostic event to pass back
     */
    public void addFailed(DiagnosticEvent diagnosticEvent)
    {
        addFailed(Collections.singletonList(diagnosticEvent));
    }

    /**
     * Adds a complete and failed progress with diagnostic events
     * @param diagnosticEvents The diagnostic events to pass back
     */
    public void addFailed(List<DiagnosticEvent> diagnosticEvents)
    {
        agendaProgressConsumer.setAgendaProgress(
            agendaProgressFactory.createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), diagnosticEvents)
        );
    }

    /**
     * Registers the progress provider for an operation
     * @param operationProgressProvider The provider to register
     */
    public void registerOperationProgressProvider(OperationProgressProvider operationProgressProvider)
    {
        agendaProgressConsumer.registerOperationProgressProvider(operationProgressProvider);
    }

    public void setOperationTotalCount(int operationTotalCount)
    {
        agendaProgressConsumer.setTotalProgressOperationCount(operationTotalCount);
    }

    public void incrementCompletedOperationCount(int incrementAmount)
    {
        agendaProgressConsumer.incrementCompletedOperationCount(incrementAmount);
    }

    public void adjustOperationTotalCount(int operationTotalCountAdjust)
    {
        agendaProgressConsumer.adjustTotalProgressOperationCount(operationTotalCountAdjust);
    }

    public AgendaProgressFactory getAgendaProgressFactory()
    {
        return agendaProgressFactory;
    }
}
