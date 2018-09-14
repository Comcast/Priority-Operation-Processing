package com.theplatform.dfh.cp.handler.puller.impl.context;

import com.theplatform.dfh.cp.handler.kubernetes.support.context.KubernetesOperationContextFactory;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AwsAgendaProviderClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.exception.PullerException;
import com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes.KubernetesLauncherFactory;
import com.theplatform.dfh.cp.handler.puller.impl.executor.local.LocalOperationLauncherFactory;
import com.theplatform.dfh.cp.handler.puller.impl.executor.LauncherFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

/**
 * Factory that creates a context object for this operation. This allows the command line to override the type of puller to use.
 */
public class PullerContextFactory extends KubernetesOperationContextFactory<PullerContext>
{

    public PullerContextFactory(LaunchDataWrapper launchDataWrapper) //, AgendaClientFactory agendaClientFactory)
    {
        super(launchDataWrapper);
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
                launcherFactory = new KubernetesLauncherFactory()
                    .setKubeConfigFactory(getKubeConfigFactory());
                break;
        }
        
        return new PullerContext(launchDataWrapper, launcherFactory); //, agendaClientFactory);
    }
}
