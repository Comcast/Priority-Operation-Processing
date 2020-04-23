package com.comcast.pop.handler.puller.impl.context;

import com.comcast.pop.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.comcast.pop.handler.puller.impl.limit.KubernetesResourceCheckerFactory;
import com.comast.pop.handler.base.context.BaseOperationContext;
import com.comcast.pop.handler.puller.impl.executor.LauncherFactory;
import com.comcast.pop.handler.puller.impl.limit.ResourceCheckerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullerContext extends BaseOperationContext<PullerLaunchDataWrapper>
{
    private static Logger logger = LoggerFactory.getLogger(PullerContext.class);

    private LauncherFactory launcherFactory;
    private ResourceCheckerFactory resourceCheckerFactory;

    public PullerContext(PullerLaunchDataWrapper launchDataWrapper, LauncherFactory launcherFactory)
    {
        super(launchDataWrapper);
        this.resourceCheckerFactory = new KubernetesResourceCheckerFactory(this);
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

    public ResourceCheckerFactory getResourceCheckerFactory()
    {
        return resourceCheckerFactory;
    }

    public PullerContext setResourceCheckerFactory(ResourceCheckerFactory resourceCheckerFactory)
    {
        this.resourceCheckerFactory = resourceCheckerFactory;
        return this;
    }
}
