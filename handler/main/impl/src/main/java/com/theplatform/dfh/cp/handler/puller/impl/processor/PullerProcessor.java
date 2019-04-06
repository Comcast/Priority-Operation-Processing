package com.theplatform.dfh.cp.handler.puller.impl.processor;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.handler.base.processor.AbstractBaseHandlerProcessor;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClient;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContext;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Basic test/local/prototype processor for getting an Agenda and sending if to the Executor
 */
public class PullerProcessor  extends AbstractBaseHandlerProcessor<PullerLaunchDataWrapper, PullerContext>
{
    private static Logger logger = LoggerFactory.getLogger(PullerProcessor.class);

    private BaseLauncher launcher;

    private AgendaClient agendaClient;

    private String insightId;
    private int agendaRequestCount = 1;

    public PullerProcessor(PullerContext pullerContext, AgendaClientFactory agendaClientFactory)
    {
        this(pullerContext);
        this.agendaClient = agendaClientFactory.getClient();
        launcher = pullerContext.getLauncherFactory().createLauncher(pullerContext);

        insightId = getLaunchDataWrapper().getPullerConfig().getInsightId();
        agendaRequestCount = getLaunchDataWrapper().getPullerConfig().getAgendaRequestCount();
    }

    public PullerProcessor(PullerContext pullerContext)
    {
        super(pullerContext);
    }


    /**
     * For testing
     * @param insightId
     */
    protected PullerProcessor(String insightId)
    {
        super(new PullerContext(null, null));
        this.insightId = insightId;
    }

    /**
     * Executes the request to get an Agenda and starts the executor pod as necessary
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

        if (getAgendaResponse == null || getAgendaResponse.getAgendas() == null)
        {
            logger.error("Failed to getAgenda"); // todo what should we do here?
            return;
        }
        else if (getAgendaResponse.isError())
        {
            logger.error("Failed to getAgenda: {}", getAgendaResponse.getErrorResponse().toString());
            return;
        }

        if(launchDataWrapper.getLastRequestAliveCheck() != null) launchDataWrapper.getLastRequestAliveCheck().updateLastRequestDate();

        Collection<Agenda> agendas = getAgendaResponse.getAgendas();

        if (agendas != null && agendas.size() > 0)
        {
            Agenda agenda = (Agenda) agendas.toArray()[0];
            logger.info("Retrieved Agenda: {}", agenda); // logs agenda hashcode?

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

    public PullerLaunchDataWrapper getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

//    public PullerProcessor setLaunchDataWrapper(PullerLaunchDataWrapper launchDataWrapper)
//    {
//        this.launchDataWrapper = launchDataWrapper;
//        return this;
//    }

    public BaseLauncher getLauncher()
    {
        return launcher;
    }

    public PullerProcessor setLauncher(BaseLauncher launcher)
    {
        this.launcher = launcher;
        return this;
    }

    public AgendaClient getAgendaClient()
    {
        return agendaClient;
    }

    public PullerProcessor setAgendaClient(AgendaClient agendaClient)
    {
        this.agendaClient = agendaClient;
        return this;
    }
}
