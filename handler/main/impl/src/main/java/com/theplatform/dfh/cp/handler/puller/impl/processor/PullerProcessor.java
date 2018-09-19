package com.theplatform.dfh.cp.handler.puller.impl.processor;

import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClient;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContext;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes.KubernetesLauncher;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic test/local/prototype processor for getting an Agenda and sending if to the Executor
 */
public class PullerProcessor implements HandlerProcessor<Void>
{
    private static Logger logger = LoggerFactory.getLogger(PullerProcessor.class);

    private PullerLaunchDataWrapper launchDataWrapper;
    private BaseLauncher launcher;

    private AgendaClient agendaClient;

    public PullerProcessor(PullerLaunchDataWrapper launchDataWrapper, PullerContext pullerContext, AgendaClientFactory agendaClientFactory)
    {
        this.launchDataWrapper = launchDataWrapper;
        this.agendaClient = agendaClientFactory.getClient();
        launcher = pullerContext.getLauncherFactory().createLauncher(pullerContext);
    }

    /**
     * Executes the ops in the Agenda in order
     * @return
     */
    public Void execute()
    {
        // todo add unit test for when agendaClient returns badness
        String agenda = agendaClient.getAgenda();

        if (agenda != null && agenda.length() > 0)
        {
            logger.info("Retrieved Agenda: {}", agenda);
            // launch an executor and pass it the agenda payload
            launcher.execute(agenda);
        }
        else
        {
            int pullWait = launchDataWrapper.getPullerConfig().getPullWait();
            logger.info("Did not retrieve Agenda. Sleeping for {} seconds.", launchDataWrapper.getPullerConfig().getPullWait());
            try
            {
                Thread.sleep(pullWait * 1000);
            }
            catch (InterruptedException e)
            {
                logger.warn("Puller execution was stopped. {}", e);
            }
        }

        return null;
    }
}
