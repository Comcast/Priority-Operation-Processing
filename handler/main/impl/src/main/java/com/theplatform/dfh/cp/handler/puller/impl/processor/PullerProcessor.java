package com.theplatform.dfh.cp.handler.puller.impl.processor;

import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClient;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContext;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes.KubernetesLauncher;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic test/local/prototype processor for running the Agenda
 */
public class PullerProcessor implements HandlerProcessor<Void>
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesLauncher.class);

    private static final String OUTPUT_SUFFIX = ".out";
    private LaunchDataWrapper launchDataWrapper;
    private PullerContext pullerContext;
    private JsonHelper jsonHelper;

    private AgendaClient agendaClient;

    public PullerProcessor(LaunchDataWrapper launchDataWrapper, PullerContext pullerContext, AgendaClient agendaClient)
    {
        this.launchDataWrapper = launchDataWrapper;
        this.pullerContext = pullerContext;
        this.jsonHelper = new JsonHelper();
        this.agendaClient = agendaClient;
    }

    /**
     * Executes the ops in the Agenda in order
     * @return
     */
    public Void execute()
    {
//        for (;;)
//        {
        String work = agendaClient.getAgenda();
        // launch an executor and pass it the agenda payload
        BaseLauncher launcher = pullerContext.getLauncherFactory().createLauncher(pullerContext);
        launcher.execute(work);
//        }

        return null;
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public void setPullerContext(PullerContext pullerContext)
    {
        this.pullerContext = pullerContext;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }
}
