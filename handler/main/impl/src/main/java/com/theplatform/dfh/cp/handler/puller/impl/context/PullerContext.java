package com.theplatform.dfh.cp.handler.puller.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.executor.LauncherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullerContext extends BaseOperationContext<PullerLaunchDataWrapper>
{
    private static Logger logger = LoggerFactory.getLogger(PullerContext.class);

    private LauncherFactory launcherFactory;

    public PullerContext(PullerLaunchDataWrapper launchDataWrapper, LauncherFactory launcherFactory)
    {
        super(launchDataWrapper);
        this.launcherFactory = launcherFactory;
    }

    @Override
    public void processUnhandledException(String s, Exception e)
    {
        logger.error(s, e);
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
