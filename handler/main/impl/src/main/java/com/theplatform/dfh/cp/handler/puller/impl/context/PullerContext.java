package com.theplatform.dfh.cp.handler.puller.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.executor.LauncherFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;

public class PullerContext extends BaseOperationContext
{
    private LauncherFactory launcherFactory;
    private PullerLaunchDataWrapper pullerLaunchDataWrapper;

    public PullerContext(PullerLaunchDataWrapper launchDataWrapper, LauncherFactory launcherFactory)
    {
        super(launchDataWrapper);
        this.launcherFactory = launcherFactory;
        this.pullerLaunchDataWrapper = launchDataWrapper;
    }

    public LauncherFactory getLauncherFactory()
    {
        return launcherFactory;
    }

    public void setLauncherFactory(LauncherFactory launcherFactory)
    {
        this.launcherFactory = launcherFactory;
    }

    public PullerLaunchDataWrapper getPullerLaunchDataWrapper()
    {
        return pullerLaunchDataWrapper;
    }

    public void setPullerLaunchDataWrapper(PullerLaunchDataWrapper pullerLaunchDataWrapper)
    {
        this.pullerLaunchDataWrapper = pullerLaunchDataWrapper;
    }
}
