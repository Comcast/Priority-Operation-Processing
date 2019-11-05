package com.theplatform.dfh.cp.handler.puller.impl.processor;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.handler.base.processor.AbstractBaseHandlerProcessor;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.PullerResourcePoolServiceClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContext;
import com.theplatform.dfh.cp.handler.puller.impl.executor.LauncherFactory;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricLabel;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaResponse;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Basic test/local/prototype processor for getting an Agenda and sending if to the Executor
 */
public class PullerProcessor extends AbstractBaseHandlerProcessor<PullerLaunchDataWrapper, PullerContext>
{
    private static Logger logger = LoggerFactory.getLogger(PullerProcessor.class);

    private PullerResourcePoolServiceClientFactory resourcePoolServiceClientFactory;
    private ResourcePoolServiceClient resourcePoolServiceClient;
    private LauncherFactory launcherFactory;

    private String insightId;
    private int agendaRequestCount = 1;
    private MetricReporter metricReporter;
    private int pullWaitSeconds = 30000;

    public PullerProcessor(PullerContext pullerContext)
    {
        super(pullerContext);
        this.resourcePoolServiceClientFactory = new PullerResourcePoolServiceClientFactory(getLaunchDataWrapper().getPullerConfig());
        launcherFactory = pullerContext.getLauncherFactory();
        insightId = getLaunchDataWrapper().getPullerConfig().getInsightId();
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
     * Initiates the endless processing loop to retrieve/start agendas.
     */
    public void execute()
    {
        while(true)
        {
            performAgendaRequest();
        }
    }

    /**
     * Executes the request to get an Agenda and starts the executor pod as necessary.
     */
    protected void performAgendaRequest()
    {
        Timer.Context timer = startTimer();

        try
        {
            pullWaitSeconds = getLaunchDataWrapper().getPullerConfig().getPullWait();
            agendaRequestCount = getLaunchDataWrapper().getPullerConfig().getAgendaRequestCount();

            GetAgendaResponse getAgendaResponse;
            try
            {
                getAgendaResponse = retrieveAgendas();
            }
            catch (Exception e)
            {
                logger.error("Failed to getAgenda: {}", e);
                failProcess();
                return;
            }

            logger.debug("PullerProcessor: AgendaResponse[" + getAgendaResponse + "], agendas[" +
                                 (getAgendaResponse == null ? "null object" : getAgendaResponse.getAgendas()) + "]");

            if (getAgendaResponse == null)
            {
                logger.error("Failed to getAgenda"); // todo what should we do here?
                failProcess();
                return;
            }
            else if (getAgendaResponse.isError())
            {
                logger.error("Failed to getAgenda: {}", getAgendaResponse.getErrorResponse().toString());
                failProcess();
                return;
            }

            logger.debug("PullerProcessor: Getting agendas from response");
            if (launchDataWrapper.getLastRequestAliveCheck() != null)
                launchDataWrapper.getLastRequestAliveCheck().updateLastRequestDate();

            Collection<Agenda> agendas = getAgendaResponse.getAgendas();

            reportSuccess();

            if (agendas != null && agendas.size() > 0)
            {
                Agenda agenda = (Agenda) agendas.toArray()[0];
                logger.info("Retrieved Agenda: {}", new JsonHelper().getJSONString(agenda)); // logs agenda hashcode?

                // launch an executor and pass it the agenda payload
                launcherFactory.createLauncher(getOperationContext()).execute(agenda);
            }
            else
            {
                pullWait();
            }
        }
        catch(InterruptedException e)
        {
            throw new RuntimeException("Thread interrupted.", e);
        }
        catch(Exception e)
        {
            logger.error("performAgendaRequest failed to process. ", e);
            try
            {
                pullWait();
            }
            catch(InterruptedException ex)
            {
                throw new RuntimeException("Thread interrupted.", ex);
            }
        }
        finally
        {
            endTimer(timer);
        }
    }

    private GetAgendaResponse retrieveAgendas()
    {
        GetAgendaRequest getAgendaRequest = new GetAgendaRequest(insightId, agendaRequestCount);
        logger.debug("PullerProcessor: Getting agenda. Request[" + getAgendaRequest + "]" +
            ", InsightId: " + getAgendaRequest.getInsightId());
        if(resourcePoolServiceClient == null)
        {
            // This does not need to be recreated repeatedly
            resourcePoolServiceClient = resourcePoolServiceClientFactory.getClient();
        }
        return resourcePoolServiceClient.getAgenda(getAgendaRequest);
    }

    private void failProcess() throws InterruptedException
    {
        reportFailure();
        pullWait();
    }

    private void pullWait() throws InterruptedException
    {
        logger.info("Did not retrieve Agenda. Sleeping for {} seconds.", pullWaitSeconds);
        Thread.sleep(pullWaitSeconds * 1000);
    }

    private Timer.Context startTimer()
    {
        return metricReporter != null ? metricReporter.timerStart(MetricLabel.duration) : null;
    }

    private void endTimer(Timer.Context timer)
    {
        if(timer != null)
            timer.stop();
    }
    private void reportSuccess()
    {
       //not going to report success for now.
    }

    private void reportFailure()
    {
        try
        {
            if (metricReporter != null)
            {
                metricReporter.mark(MetricLabel.failed);
            }
        }
        catch (Throwable e)
        {
            logger.error("Failure getting metric for reporting.", e);
        }
    }
    public PullerLaunchDataWrapper getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public PullerResourcePoolServiceClientFactory getResourcePoolServiceClientFactory()
    {
        return resourcePoolServiceClientFactory;
    }

    public LauncherFactory getLauncherFactory()
    {
        return launcherFactory;
    }

    public PullerProcessor setLauncherFactory(LauncherFactory launcherFactory)
    {
        this.launcherFactory = launcherFactory;
        return this;
    }

    public PullerProcessor setResourcePoolServiceClientFactory(PullerResourcePoolServiceClientFactory resourcePoolServiceClientFactory)
    {
        this.resourcePoolServiceClientFactory = resourcePoolServiceClientFactory;
        return this;
    }

    public void setMetricReporter(MetricReporter metricReporter)
    {
        if(metricReporter != null)
        {
            this.metricReporter = metricReporter;
        }
    }
}
