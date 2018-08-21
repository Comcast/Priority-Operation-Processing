package com.theplatform.dfh.cp.handler.sample.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.ReporterSet;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporter;
import com.theplatform.dfh.cp.handler.reporter.log.LogReporter;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.OAuthCredentialCapture;

/**
 * Factory that creates a context object for this operation. This allows the command line to override the type of executor to use.
 */
public class OperationContextFactory extends BaseOperationContextFactory<OperationContext>
{
    public OperationContextFactory(LaunchDataWrapper launchDataWrapper)
    {
        super(launchDataWrapper);
    }

    @Override
    public OperationContext createOperationContext()
    {
        switch (getLaunchType())
        {
            case local:
                return new OperationContext(new LogReporter(), launchDataWrapper);
            case docker:
                return new OperationContext(new LogReporter(), launchDataWrapper);
            case kubernetes:
            default:
                return getKubernetesContext();

        }
    }

    public OperationContext getKubernetesContext()
    {
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
        return new OperationContext(reporterSet, launchDataWrapper);
    }
}
