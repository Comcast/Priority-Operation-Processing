package com.comcast.pop.handler.executor.impl.progress.agenda;

import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comcast.pop.api.progress.ProcessingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Factory for creating AgendaProgress
 */
public class AgendaProgressFactory
{
    private static Logger logger = LoggerFactory.getLogger(AgendaProgressFactory.class);

    private String agendaProgressId;
    private Date agendaStartedTime;

    public AgendaProgressFactory(String agendaProgressId)
    {
        this.agendaProgressId = agendaProgressId;
        // TODO: this only needs to be transmitted once technically...
        this.agendaStartedTime = new Date();
    }

    /**
     * Creates an AgendaProgress with the specified state and message
     * @param processingState The state to set on the AgendaProgress
     * @param processingStateMessage The message to set on the AgendaProgress
     * @return The new AgendaProgress
     */
    public AgendaProgress createAgendaProgress(ProcessingState processingState, String processingStateMessage)
    {
        return createAgendaProgress(processingState, processingStateMessage, null);
    }

    /**
     * Creates an AgendaProgress with the specified state and message
     * @param processingState The state to set on the AgendaProgress
     * @param processingStateMessage The message to set on the AgendaProgress
     * @return The new AgendaProgress
     */
    public AgendaProgress createAgendaProgress(ProcessingState processingState, String processingStateMessage, List<DiagnosticEvent> diagnosticEvents)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setId(agendaProgressId);
        // TODO: remove this when we are persisting it on progress create
        agendaProgress.setProcessingState(processingState);
        agendaProgress.setProcessingStateMessage(processingStateMessage);
        agendaProgress.setStartedTime(agendaStartedTime);
        agendaProgress.setDiagnosticEvents(diagnosticEvents == null ? null : diagnosticEvents.toArray(new DiagnosticEvent[0]));
        switch (processingState)
        {
            // TODO: should the AgendaProgress endpoint deal with all time values?
            case COMPLETE:
                agendaProgress.setCompletedTime(new Date());
                break;
        }
        return agendaProgress;
    }
}
