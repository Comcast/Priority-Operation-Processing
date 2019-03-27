package com.theplatform.dfh.cp.handler.puller.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.executor.LauncherFactory;

public class PullerContext extends BaseOperationContext<PullerLaunchDataWrapper>
{
    private LauncherFactory launcherFactory;

    public PullerContext(PullerLaunchDataWrapper launchDataWrapper, LauncherFactory launcherFactory)
    {
        super(launchDataWrapper);
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
