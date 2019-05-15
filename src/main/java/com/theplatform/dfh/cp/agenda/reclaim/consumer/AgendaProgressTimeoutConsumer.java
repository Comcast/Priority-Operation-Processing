package com.theplatform.dfh.cp.agenda.reclaim.consumer;

import com.theplatform.com.dfh.modules.sync.util.Consumer;
import com.theplatform.com.dfh.modules.sync.util.ConsumerResult;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;

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
        int reclaimCount = 0;
        for(AgendaProgress agendaProgress : collection)
        {
            if(updateAgendaProgress(agendaProgress))
                reclaimCount++;
        }
        return consumerResult.setItemsConsumedCount(reclaimCount);
    }

    protected boolean updateAgendaProgress(AgendaProgress agendaProgress)
    {
        AgendaProgress updatedAgendaProgress = new AgendaProgress();
        updatedAgendaProgress.setId(agendaProgress.getId());
        updatedAgendaProgress.setProcessingState(ProcessingState.COMPLETE);
        updatedAgendaProgress.setProcessingStateMessage(CompleteStateMessage.FAILED.toString());
        updatedAgendaProgress.setDiagnosticEvents(new DiagnosticEvent[]{
            new DiagnosticEvent()
            .withMessage("AgendaProgress timed out.")
        });
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
