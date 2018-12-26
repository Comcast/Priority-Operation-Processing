package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.cp.scheduling.api.AgendaInfo;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.modules.queue.api.ItemQueue;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.api.QueueResult;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Agenda service request processor
 */
public class AgendaServiceRequestProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(AgendaServiceRequestProcessor.class);

    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<Agenda> agendaPersister;
    private ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory;

    public AgendaServiceRequestProcessor(ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory, ObjectPersister<Insight> insightPersister,
        ObjectPersister<Agenda> agendaPersister)
    {
        this.agendaInfoItemQueueFactory = agendaInfoItemQueueFactory;
        this.insightPersister = insightPersister;
        this.agendaPersister = agendaPersister;
    }

    public GetAgendaResponse processRequest(GetAgendaRequest getAgendaRequest)
    {
        // TODO: input validation
        if (getAgendaRequest.getInsightId() == null)
        {
            logger.warn("No insight id provided.  Cannot process getAgenda request.");
            return null;
        }

        Insight insight;
        try
        {
            insight = insightPersister.retrieve(getAgendaRequest.getInsightId());
        }
        catch(PersistenceException e)
        {
            // TODO: standardize our result objects (do they use an exception field, error field, etc.)
            logger.warn("Could not receive Insight.", e);
            return null;
        }

        if(insight == null)
        {
            logger.warn("No insight found with id {}. Cannot process getAgenda request.", getAgendaRequest.getInsightId());
            return null;
        }

        try
        {
            QueueResult<AgendaInfo> agendaInfoQueueResult = pollInsightQueue(insight, getAgendaRequest.getCount());
            if(agendaInfoQueueResult.isSuccessful())
            {
                List<Agenda> agendaList = new LinkedList<>();
                // TODO: if the results include more than the desired amount cap it
                // TODO: this is not optimal, a multi-get would be better...
                if(agendaInfoQueueResult.getData() != null)
                {
                    for (AgendaInfo agendaInfo : agendaInfoQueueResult.getData())
                    {
                        Agenda agenda = retrieveAgenda(agendaInfo.getAgendaId());
                        if (agenda == null)
                        {
                            logger.warn("Could not find Agenda with id {}", agendaInfo.getAgendaId());
                        }  else
                        {
                            agendaList.add(agenda);
                        }
                    }
                }
                return createAgendaServiceResult(agendaList);
            }
            else
            {
                logger.error("Failed to poll queue for AgendaInfo.");
                return null;
            }
        }
        catch(PersistenceException e)
        {
            logger.error("Failure getting Agenda(s).", e);
        }
        catch(BadRequestException e)
        {
            logger.error("Failure getting Agenda(s).", e);
        }
        return null;
    }

    private GetAgendaResponse createAgendaServiceResult(Collection<Agenda> agendas)
    {
        return new GetAgendaResponse(agendas);
    }

    public Agenda retrieveAgenda(String agendaId) throws PersistenceException
    {
        return agendaPersister.retrieve(agendaId);
    }

    /**
     * Polls the queue associated with the specified insight for a number of results.
     * @param insight The insight to poll the queue for
     * @param maxResults The maximum number of results to take from the queue
     * @return A QueueResult
     */
    public QueueResult<AgendaInfo> pollInsightQueue(Insight insight, int maxResults) throws BadRequestException
    {
        if(maxResults < 1)
        {
            throw new BadRequestException(String.format("The maximum results specified is not supported: %1$s", maxResults));
        }
        // TODO: might be an insight id and we have an insight client
        ItemQueue<AgendaInfo> itemQueue = agendaInfoItemQueueFactory.createItemQueue(insight.getQueueName());
        return itemQueue.poll(maxResults);
    }

    public void setInsightPersister(ObjectPersister<Insight> insightPersister)
    {
        this.insightPersister = insightPersister;
    }

    public void setAgendaPersister(ObjectPersister<Agenda> agendaPersister)
    {
        this.agendaPersister = agendaPersister;
    }

    public void setAgendaInfoItemQueueFactory(ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory)
    {
        this.agendaInfoItemQueueFactory = agendaInfoItemQueueFactory;
    }
}
