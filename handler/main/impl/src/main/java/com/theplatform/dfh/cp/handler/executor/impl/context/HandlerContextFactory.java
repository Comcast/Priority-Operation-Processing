package com.theplatform.dfh.cp.handler.executor.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes.KubernetesOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.local.LocalOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.OperationExecutorFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;
import com.theplatform.dfh.cp.handler.reporter.api.ReporterSet;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporter;
import com.theplatform.dfh.cp.handler.reporter.log.LogReporter;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.OAuthCredentialCapture;

/**
 * Factory that creates a context object for this operation. This allows the command line to override the type of executor to use.
 */
public class HandlerContextFactory extends BaseOperationContextFactory<HandlerContext>
{

    public HandlerContextFactory(LaunchDataWrapper launchDataWrapper)
    {
        super(launchDataWrapper);
    }

    @Override
    public HandlerContext getOperationContext()
    {
        Reporter reporter;
        OperationExecutorFactory operationExecutorFactory;

        switch(getLaunchType())
        {
            case local:
            case docker:
                reporter = new LogReporter();
                break;
            case kubernetes:
            default:
                reporter = getKubernetesReporterSet();
                break;
        }

        switch (getExternalLaunchType())
        {
            case local:
                operationExecutorFactory = new LocalOperationExecutorFactory();
                break;
            case docker:
                // TODO: decide if we want to support docker ops execution...
                throw new AgendaExecutorException("Docker is not supported for agenda execution.");
            case kubernetes:
            default:
                operationExecutorFactory = new KubernetesOperationExecutorFactory();
                break;
        }

        return new HandlerContext(reporter, launchDataWrapper, operationExecutorFactory);
    }

    public ReporterSet getKubernetesReporterSet()
    {
        // This is duplicated in the sample handler... undupe it!
        ReporterSet reporterSet = new ReporterSet();
        reporterSet.add(new LogReporter());

        KubeConfig kubeConfig = new KubeConfig();

        OAuthCredentialCapture oauthCredentialCapture = new OAuthCredentialCapture().init();
        if (oauthCredentialCapture.isOAuthAvailable())
        {
            kubeConfig.setCaCertData(oauthCredentialCapture.getOauthCert());
            kubeConfig.setOauthToken(oauthCredentialCapture.getOauthToken());
        }

        // hard coded for now... (the master url should probably come from a config map, through pass through is nice...)
        kubeConfig.setNameSpace("dfh");
        kubeConfig.setMasterUrl(launchDataWrapper.getEnvironmentRetriever().getField("K8_MASTER_URL"));

        reporterSet.add(new KubernetesReporter(
            kubeConfig,
            launchDataWrapper.getEnvironmentRetriever().getField(Fabric8Helper.MY_POD_NAME))
        );
        return reporterSet;
    }
}
