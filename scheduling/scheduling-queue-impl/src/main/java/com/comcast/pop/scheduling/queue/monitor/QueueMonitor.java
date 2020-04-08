package com.comcast.pop.scheduling.queue.monitor;

import com.comcast.pop.scheduling.queue.algorithm.AgendaScheduler;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.scheduling.queue.InsightScheduleInfo;
import com.comcast.pop.scheduling.queue.algorithm.AgendaSchedulerFactory;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.query.resourcepool.insight.ByResourcePoolId;
import com.comcast.pop.endpoint.client.ObjectClient;
import com.comcast.pop.modules.queue.api.ItemQueue;
import com.comcast.pop.modules.queue.api.ItemQueueFactory;
import com.comcast.pop.modules.queue.api.QueueResult;
import com.comcast.pop.persistence.api.ObjectPersister;
import com.comcast.pop.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class QueueMonitor
{
    private final static Logger logger = LoggerFactory.getLogger(QueueMonitor.class);
    private final static String METRIC_SKIPPED = "skipped";
    private final static String METRIC_WAITING = "waiting";
    private AgendaSchedulerFactory agendaSchedulerFactory = new AgendaSchedulerFactory();

    private ItemQueueFactory<ReadyAgenda> readyAgendaQueueFactory;
    private ObjectClient<Insight> insightClient;
    private ObjectPersister<InsightScheduleInfo> insightScheduleInfoPersister;

    private ObjectPersister<ReadyAgenda> readyAgendaPersister;
    private ObjectClient<Customer> customerClient;

    public QueueMonitor(
        ItemQueueFactory<ReadyAgenda> readyAgendaQueueFactory,
        ObjectPersister<ReadyAgenda> readyAgendaPersister,
        ObjectClient<Insight> insightClient,
        ObjectClient<Customer> customerClient,
        ObjectPersister<InsightScheduleInfo> insightScheduleInfoPersister)
    {
        this.readyAgendaQueueFactory = readyAgendaQueueFactory;
        this.readyAgendaPersister = readyAgendaPersister;
        this.insightClient = insightClient;
        this.customerClient = customerClient;
        this.insightScheduleInfoPersister = insightScheduleInfoPersister;
    }

    public void processResourcePool(String resourcePoolId) throws Throwable
    {
        DataObjectResponse<Insight> insightObjectFeed =
            insightClient.getObjects(Collections.singletonList(new ByResourcePoolId(resourcePoolId)));
        if(insightObjectFeed.isError())
        {
            logger.error("Error getting any insights by resourcePoolId: {}", resourcePoolId);
            return;
        }
        if(insightObjectFeed.getAll() == null || insightObjectFeed.getAll().size() == 0)
        {
            logger.info("No insights found for ResourcePool: {}", resourcePoolId);
            return;
        }

        for(Insight insight : insightObjectFeed.getAll())
        {
            try
            {
                InsightScheduleInfo insightScheduleInfo = retrieveInsightSchedulingInfo(resourcePoolId, insight.getId());
                processInsight(insight, insightScheduleInfo);

                // TODO: break into own method?
                insightScheduleInfo.setLastExecuted(new Date());
                insightScheduleInfoPersister.persist(insightScheduleInfo);
                logger.info("Persisted update to InsightScheduleInfo: {}", insightScheduleInfo.getId());
            }
            catch(Exception e)
            {
                logger.error("Failed to process ResourcePool: {} Insight: {}", resourcePoolId, insight.getId(), e);
            }
        }
        logger.info("Processed ResourcePool: {}", resourcePoolId);
    }

    protected void processInsight(Insight insight, InsightScheduleInfo insightScheduleInfo)
    {
        ItemQueue<ReadyAgenda> readyAgendaQueue = readyAgendaQueueFactory.createItemQueue(insight.getQueueName());
        QueueResult queueResult = readyAgendaQueue.size();
        if(!queueResult.isSuccessful())
        {
            // TODO: what exactly?
            logger.error("Failed to get queue size for queue: {} from insight: {}", insight.getQueueName(), insight.getId());
            return;
        }
        final int itemsOnQueueCount = Integer.parseInt(queueResult.getMessage());
        final int maxQueueSize = insight.getQueueSize();
        if(itemsOnQueueCount >= maxQueueSize)
        {
            // TODO: human readable insight name might be helpful...
            logger.info("Queue for insight {} does not require queue. Min: {} Current: {}", insight.getId(), maxQueueSize, itemsOnQueueCount);
            return;
        }
        else
        {
            logger.info("Queue for insight {} requires queue. Min: {} Current: {}", insight.getId(), maxQueueSize, itemsOnQueueCount);
        }

        int requestedCount = maxQueueSize - itemsOnQueueCount;

        AgendaScheduler agendaScheduler = agendaSchedulerFactory.getAgendaScheduler(insight, readyAgendaPersister, customerClient);

        Collection<ReadyAgenda> readyAgendaCollection = agendaScheduler.schedule(requestedCount, insight, insightScheduleInfo);

        int itemsQueued = 0;

        if(readyAgendaCollection != null && readyAgendaCollection.size() > 0)
        {
           for(ReadyAgenda readyAgenda : readyAgendaCollection)
           {
               if(moveReadyAgenda(readyAgenda, readyAgendaQueue))
               {
                   itemsQueued++;
               }
           }
        }
        logger.info("Insight: {} Requested: {}  Queued: {}", insight.getId(), requestedCount, itemsQueued);
    }

    boolean moveReadyAgenda(ReadyAgenda readyAgenda, ItemQueue<ReadyAgenda> readyAgendaQueue)
    {
        logger.info("Queuing ReadyAgenda InsightId: {} AgendaId: {} CustomerId: {}", readyAgenda.getInsightId(), readyAgenda.getAgendaId(), readyAgenda.getCustomerId());
        try
        {
            readyAgendaPersister.delete(readyAgenda.getId());
        }
        catch(PersistenceException e)
        {
            // TODO: should we break the entire process because a delete failed? (suggest: no)
            logger.error("Failed to delete ReadyAgenda: {} Skipping queueing attempt.", readyAgenda.getId());
            return false;
        }
        QueueResult<ReadyAgenda> queueResult = readyAgendaQueue.add(readyAgenda);
        if(queueResult.isSuccessful())
        {
            return true;
        }
        else
        {
            // TODO: rollback attempt? put the item back in the ReadyAgenda table?
            logger.error("Failed to queue ReadyAgenda: {} Attempting rollback of table remove.", readyAgenda.getId());

            try
            {
                readyAgendaPersister.persist(readyAgenda);
            }
            catch (PersistenceException e)
            {
                logger.error("Failed rollback of ReadyAgenda: {}. Repair required for agenda: {}", readyAgenda.getId(), readyAgenda.getAgendaId());
                return false;
            }
        }
        return false;
    }

    InsightScheduleInfo retrieveInsightSchedulingInfo(String resourcePoolId, String insightId) throws PersistenceException
    {
        final String insightSchedulingInfoId = InsightScheduleInfo.generateId(resourcePoolId, insightId);
        logger.info("Retrieving InsightSchedulingInfo: {}", insightSchedulingInfoId);

        InsightScheduleInfo insightScheduleInfo =
            insightScheduleInfoPersister.retrieve(insightSchedulingInfoId);
        // if none was found create a new one for use in queue (and for later persistence)
        if(insightScheduleInfo == null)
        {
            logger.info("No InsightSchedulingInfo found. Generating a new one for: {}", insightSchedulingInfoId);
            insightScheduleInfo = new InsightScheduleInfo();
            insightScheduleInfo.setId(InsightScheduleInfo.generateId(resourcePoolId, insightId));
        }
        else
        {
            logger.info("InsightSchedulingInfo found for: {} lastUpdated: {}", insightScheduleInfo.getLastExecuted());
        }
        return insightScheduleInfo;
    }

    public void setAgendaSchedulerFactory(AgendaSchedulerFactory agendaSchedulerFactory)
    {
        this.agendaSchedulerFactory = agendaSchedulerFactory;
    }
}
