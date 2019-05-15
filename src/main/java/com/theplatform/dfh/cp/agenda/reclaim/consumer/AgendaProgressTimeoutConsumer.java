package com.theplatform.dfh.cp.agenda.reclaim.consumer;

import com.theplatform.com.dfh.modules.sync.util.Consumer;
import com.theplatform.com.dfh.modules.sync.util.ConsumerResult;
import com.theplatform.com.dfh.modules.sync.util.InstantUtil;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * AgendaProgress
 */
public class AgendaProgressTimeoutConsumer implements Consumer<AgendaProgress>
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HttpObjectClient<AgendaProgress> agendaProgressClient;

    public AgendaProgressTimeoutConsumer(HttpObjectClient<AgendaProgress> agendaProgressClient)
    {
        this.agendaProgressClient = agendaProgressClient;
    }

    @Override
    public ConsumerResult<AgendaProgress> consume(Collection<AgendaProgress> collection, Instant endProcessingInstant)
    {
        ConsumerResult<AgendaProgress> consumerResult = new ConsumerResult<>();

        if(collection == null)
            return consumerResult;

        int reclaimCount = 0;
        for(AgendaProgress agendaProgress : collection)
        {
            // TODO: if/when batches of updates are supported process more than 1 at a time
            if(updateAgendaProgress(agendaProgress))
                reclaimCount++;

            if(InstantUtil.isNowAfterOrEqual(endProcessingInstant))
                break;

        }
        return consumerResult.setItemsConsumedCount(reclaimCount);
    }

    protected boolean updateAgendaProgress(AgendaProgress agendaProgress)
    {
        AgendaProgress updatedAgendaProgress = new AgendaProgress();
        updatedAgendaProgress.setId(agendaProgress.getId());
        updatedAgendaProgress.setProcessingState(ProcessingState.COMPLETE);
        updatedAgendaProgress.setProcessingStateMessage(CompleteStateMessage.FAILED.toString());
        DiagnosticEvent diagnosticEvent = new DiagnosticEvent()
                .withMessage("AgendaProgress timed out.");

        DiagnosticEvent[] existingDiagnosticEvents = agendaProgress.getDiagnosticEvents();
        if(existingDiagnosticEvents == null)
            updatedAgendaProgress.setDiagnosticEvents(new DiagnosticEvent[]{ diagnosticEvent });
        else
        {
            LinkedList<DiagnosticEvent> existingEvents = new LinkedList<>(Arrays.asList(existingDiagnosticEvents));
            existingEvents.add(diagnosticEvent);
            updatedAgendaProgress.setDiagnosticEvents(existingEvents.toArray(new DiagnosticEvent[0]));
        }

        try
        {
            agendaProgressClient.updateObject(updatedAgendaProgress, updatedAgendaProgress.getId());
        }
        catch (Exception e)
        {
            logger.error(String.format("Failed to reclaim Agenda: %1$s", agendaProgress.getAgendaId()), e);
            return false;
        }
        return true;
    }
}
