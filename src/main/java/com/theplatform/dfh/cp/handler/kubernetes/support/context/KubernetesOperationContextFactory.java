package com.theplatform.dfh.cp.handler.kubernetes.support.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactoryImpl;
import com.theplatform.dfh.cp.handler.reporter.api.ProgressReporter;
import com.theplatform.dfh.cp.handler.reporter.api.ProgressReporterSet;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporter;
import com.theplatform.dfh.cp.handler.reporter.log.LogReporter;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;

/**
 * Factory that creates a context object for the handler with kubernetes specific support
 */
public abstract class KubernetesOperationContextFactory<T extends BaseOperationContext> extends BaseOperationContextFactory<T>
{
    private KubeConfigFactory kubeConfigFactory;

    public KubernetesOperationContextFactory(LaunchDataWrapper launchDataWrapper)
    {
        super(launchDataWrapper);
        this.kubeConfigFactory = new KubeConfigFactoryImpl(launchDataWrapper);
    }

    /**
     * Returns a reporter based on the launch type
     * @return The reporter to use for the execution of this handler
     */
    protected ProgressReporter createReporter()
    {
        switch(getLaunchType())
        {
            case local:
            case docker:
                return new LogReporter();
            case kubernetes:
            default:
                return createKubernetesReporter();
        }
    }

    /**
     * Creates a Kubernetes specific reporter
     * @return
     */
    protected ProgressReporterSet createKubernetesReporter()
    {
        // This is duplicated in the sample handler... undupe it!
        ProgressReporterSet reporterSet = new ProgressReporterSet();
        reporterSet.add(new LogReporter());

        KubeConfig kubeConfig = kubeConfigFactory.createKubeConfig();

        reporterSet.add(new KubernetesReporter(
            kubeConfig,
            launchDataWrapper.getEnvironmentRetriever().getField(Fabric8Helper.MY_POD_NAME))
        );
        return reporterSet;
    }

    public KubeConfigFactory getKubeConfigFactory()
    {
        return kubeConfigFactory;
    }

    public void setKubeConfigFactory(KubeConfigFactory kubeConfigFactory)
    {
        this.kubeConfigFactory = kubeConfigFactory;
    }
}
