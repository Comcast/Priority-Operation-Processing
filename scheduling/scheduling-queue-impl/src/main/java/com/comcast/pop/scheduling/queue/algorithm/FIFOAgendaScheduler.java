package com.comcast.pop.scheduling.queue.algorithm;

import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.scheduling.queue.InsightScheduleInfo;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.data.query.resourcepool.insight.ByInsightId;
import com.comcast.pop.persistence.api.DataObjectFeed;
import com.comcast.pop.persistence.api.ObjectPersister;
import com.comcast.pop.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FIFOAgendaScheduler implements AgendaScheduler
{
    private final static Logger logger = LoggerFactory.getLogger(FIFOAgendaScheduler.class);

    private final ObjectPersister<ReadyAgenda> readyAgendaPersister;

    public FIFOAgendaScheduler(ObjectPersister<ReadyAgenda> readyAgendaPersister)
    {
        this.readyAgendaPersister = readyAgendaPersister;
    }

    @Override
    public List<ReadyAgenda> schedule(int requestCount, Insight insight, InsightScheduleInfo insightScheduleInfo)
    {
        DataObjectFeed<ReadyAgenda> readyAgendaFeed;
        try
        {
            readyAgendaFeed = readyAgendaPersister.retrieve(Arrays.asList(new ByInsightId(insight.getId())));
        }
        catch(PersistenceException e)
        {
            logger.error("Failed to retrieve ReadyAgendas for queue.", e);
            // todo: anything
            return null;
        }

        if(readyAgendaFeed.isError())
        {
            logger.error("ReadyAgenda feed returned with an error.", readyAgendaFeed.getException());
            return null;
        }

        List<ReadyAgenda> readyAgendas = readyAgendaFeed.getAll();
        List<ReadyAgenda> scheduledReadyAgendas = new ArrayList<>();
        if(readyAgendas.size() > 0)
        {
            scheduledReadyAgendas = readyAgendas.subList(0, Math.min(requestCount, readyAgendas.size()));
        }

        logger.info("FIFO Result -- InsightId: {} Scheduled: {} Requested: {}",
            insight.getId(), scheduledReadyAgendas.size(), requestCount);

        return scheduledReadyAgendas;
    }
}
