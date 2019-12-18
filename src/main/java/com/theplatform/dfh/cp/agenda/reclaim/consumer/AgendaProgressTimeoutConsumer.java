package com.theplatform.dfh.cp.agenda.reclaim.consumer;

import com.theplatform.com.dfh.modules.sync.util.Consumer;
import com.theplatform.com.dfh.modules.sync.util.ConsumerResult;
import com.theplatform.com.dfh.modules.sync.util.InstantUtil;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * AgendaProgress timed out consumer
 */
public class AgendaProgressTimeoutConsumer implements Consumer<String>
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HttpObjectClient<AgendaProgress> agendaProgressClient;
    private boolean logReclaimOnly = false;

    public AgendaProgressTimeoutConsumer(HttpObjectClient<AgendaProgress> agendaProgressClient)
    {
        this.agendaProgressClient = agendaProgressClient;
    }

    @Override
    public ConsumerResult<String> consume(Collection<String> collection, Instant endProcessingInstant)
    {
        ConsumerResult<String> consumerResult = new ConsumerResult<>();

        if(collection == null)
            return consumerResult;

        int reclaimCount = 0;
        for(String agendaProgressId : collection)
        {
            // TODO: if/when batches of updates are supported process more than 1 at a time
            if(updateAgendaProgress(agendaProgressId))
                reclaimCount++;

            if(InstantUtil.isNowAfterOrEqual(endProcessingInstant))
                break;

        }
        return consumerResult.setItemsConsumedCount(reclaimCount);
    }

    protected boolean updateAgendaProgress(String agendaProgressId)
    {
        // TODO: actually get the AgendaProgress so we can append DiagnosticEvents instead of just overwriting them
        AgendaProgress agendaProgress = retrieveAgendaProgress(agendaProgressId);
        if(agendaProgress == null)
            return false;

        AgendaProgress updatedAgendaProgress = new AgendaProgress();
        updatedAgendaProgress.setId(agendaProgressId);
        updatedAgendaProgress.setCustomerId(agendaProgress.getCustomerId());
        updatedAgendaProgress.setProcessingState(ProcessingState.COMPLETE);
        updatedAgendaProgress.setProcessingStateMessage(CompleteStateMessage.FAILED.toString());
        DiagnosticEvent diagnosticEvent = new DiagnosticEvent("AgendaProgress timed out.");

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
            logger.info("Reclaiming Agenda: {} Updating AgendaProgress: {}", agendaProgress.getAgendaId(), updatedAgendaProgress.getId());
            if(!logReclaimOnly)
                agendaProgressClient.updateObject(updatedAgendaProgress, updatedAgendaProgress.getId());
        }
        catch (Exception e)
        {
            logger.error(String.format("Failed to reclaim Agenda: %1$s", agendaProgress.getAgendaId()), e);
            return false;
        }
        return true;
    }

    protected AgendaProgress retrieveAgendaProgress(String agendaProgressId)
    {
        try
        {
            DataObjectResponse<AgendaProgress> response = agendaProgressClient.getObject(agendaProgressId);
            if(!response.isError())
            {
                if(response.getAll()!= null && response.getAll().size() > 0)
                    return response.getFirst();
                else
                    logger.error("AgendaProgress no longer exists: {} {} ", agendaProgressId, response.getErrorResponse());
            }
            logger.error("Failed to retrieve AgendaProgress: {} {} ", agendaProgressId, response.getErrorResponse());
        }
        catch (Exception e)
        {
            logger.error(String.format("Failed to retrieve AgendaProgress: %1$s", agendaProgressId), e);
        }
        return null;
    }

    public boolean getLogReclaimOnly()
    {
        return logReclaimOnly;
    }

    public AgendaProgressTimeoutConsumer setLogReclaimOnly(boolean logReclaimOnly)
    {
        this.logReclaimOnly = logReclaimOnly;
        return this;
    }
}
