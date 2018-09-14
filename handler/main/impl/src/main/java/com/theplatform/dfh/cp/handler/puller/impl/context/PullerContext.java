package com.theplatform.dfh.cp.handler.puller.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.executor.LauncherFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;

public class PullerContext extends BaseOperationContext
{
    private LauncherFactory launcherFactory;

    public PullerContext(LaunchDataWrapper launchDataWrapper, LauncherFactory launcherFactory) //, AgendaClientFactory agendaClientFactory)
    {
        super(null, launchDataWrapper);
        this.launcherFactory = launcherFactory;
    }

    public LauncherFactory getLauncherFactory()
    {
        return launcherFactory;
    }

    public void setLauncherFactory(LauncherFactory launcherFactory)
    {
        this.launcherFactory = launcherFactory;
    }
}
