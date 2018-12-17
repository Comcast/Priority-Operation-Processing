package com.theplatform.dfh.cp.handler.puller.impl.processor;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContext;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class PullerWithInsightProcessor extends PullerProcessor
{

    private static Logger logger = LoggerFactory.getLogger(PullerWithInsightProcessor.class);

    private int agendaRequestCount = 1;
    private String insightId;

    public PullerWithInsightProcessor(PullerContext pullerContext, AgendaClientFactory agendaClientFactory)
    {
        super(pullerContext, agendaClientFactory);
        insightId = getLaunchDataWrapper().getPullerConfig().getInsightId();
        agendaRequestCount = getLaunchDataWrapper().getPullerConfig().getAgendaRequestCount();
    }

    protected PullerWithInsightProcessor(String insightId)
    {
        super();
        this.insightId = insightId;
    }

    /**
     * Executes the ops in the Agenda in order
     */
    public void execute()
    {
        GetAgendaRequest getAgendaRequest = new GetAgendaRequest(insightId, agendaRequestCount);
        GetAgendaResponse getAgendaResponse;
        try
        {
            getAgendaResponse = getAgendaClient().getAgenda(getAgendaRequest);
        } catch (Exception e)
        {
            logger.error("Failed to getAgenda: {}", e);
            return;
        }
        // todo what to do if getAgenda returns null?

        Collection<Agenda> agendas = getAgendaResponse.getAgendas();

        if (agendas != null && agendas.size() > 0)
        {
            Agenda agenda = (Agenda) agendas.toArray()[0];
            logger.info("Retrieved Agenda: {}", agenda);
            // launch an executor and pass it the agenda payload
            getLauncher().execute(agenda);
        }
        else
        {
            int pullWait = getLaunchDataWrapper().getPullerConfig().getPullWait();
            logger.info("Did not retrieve Agenda. Sleeping for {} seconds.", getLaunchDataWrapper().getPullerConfig().getPullWait());
            try
            {
                Thread.sleep(pullWait * 1000);
            }
            catch (InterruptedException e)
            {
                logger.warn("Puller execution was stopped. {}", e);
            }
        }
    }
}
