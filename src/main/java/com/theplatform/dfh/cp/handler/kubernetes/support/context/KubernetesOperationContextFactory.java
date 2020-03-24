package com.theplatform.dfh.cp.handler.kubernetes.support.context;

import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.reporter.LogReporter;
import com.theplatform.dfh.cp.handler.base.reporter.ProgressReporter;
import com.theplatform.dfh.cp.handler.base.reporter.ProgressReporterSet;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactoryImpl;
import com.theplatform.dfh.cp.handler.kubernetes.support.reporter.KubernetesReporter;
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
    protected ProgressReporter<OperationProgress> createReporter()
    {
        switch(getLaunchType())
        {
            case local:
            case docker:
                return new LogReporter<>();
            case kubernetes:
            default:
                return createKubernetesReporter();
        }
    }

    /**
     * Creates a Kubernetes specific reporter
     * @return Reporter set with both the log and kubernetes reporter
     */
    protected ProgressReporterSet<OperationProgress> createKubernetesReporter()
    {
        ProgressReporterSet<OperationProgress> reporterSet = new ProgressReporterSet<>();
        reporterSet.add(new LogReporter<>());

        KubeConfig kubeConfig = kubeConfigFactory.createKubeConfig();

        reporterSet.add(new KubernetesReporter<>(
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
