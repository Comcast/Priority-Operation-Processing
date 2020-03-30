package com.comcast.fission.handler.puller.impl.context;

import com.comcast.fission.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.kubernetes.support.context.KubernetesOperationContextFactory;
import com.comcast.fission.handler.puller.impl.exception.PullerException;
import com.comcast.fission.handler.puller.impl.executor.kubernetes.KubernetesLauncherFactory;
import com.comcast.fission.handler.puller.impl.executor.local.LocalOperationLauncherFactory;
import com.comcast.fission.handler.puller.impl.executor.LauncherFactory;

/**
 * Factory that creates a context object for this operation. This allows the command line to override the type of puller to use.
 */
public class PullerContextFactory extends KubernetesOperationContextFactory<PullerContext>
{
    private PullerLaunchDataWrapper pullerLaunchDataWrapper;

    public PullerContextFactory(PullerLaunchDataWrapper launchDataWrapper)
    {
        super(launchDataWrapper);
        this.pullerLaunchDataWrapper = launchDataWrapper;
    }

    @Override
    public PullerContext createOperationContext()
    {
        LauncherFactory launcherFactory;

        switch (getExternalLaunchType())
        {
            case local:
                launcherFactory = new LocalOperationLauncherFactory();
                break;
            case docker:
                throw new PullerException("Docker is not supported for agenda execution.");
            case kubernetes:
            default:
                launcherFactory = new KubernetesLauncherFactory(pullerLaunchDataWrapper)
                    .setKubeConfigFactory(getKubeConfigFactory());
                break;
        }
        
        return new PullerContext(pullerLaunchDataWrapper, launcherFactory);
    }
}
