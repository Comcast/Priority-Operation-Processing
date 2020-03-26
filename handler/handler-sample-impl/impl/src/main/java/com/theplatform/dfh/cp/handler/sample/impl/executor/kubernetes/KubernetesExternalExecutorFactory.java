package com.theplatform.dfh.cp.handler.sample.impl.executor.kubernetes;

import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.NfsDetailsFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.PodConfigFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.PodConfigFactoryImpl;
import com.theplatform.dfh.cp.handler.sample.impl.executor.BaseExternalExecutor;
import com.theplatform.dfh.cp.handler.sample.impl.executor.ExternalExecutorFactory;
import com.theplatform.dfh.cp.modules.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.NfsDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Factory for producing executors to get MediaProperties (via a mediainfo launch through kubernetes)
 */
public class KubernetesExternalExecutorFactory implements ExternalExecutorFactory
{
    private static final String NFS_PROPERTIES_PREFIX = "fission.handler.sample.nfs.";
    private static Logger logger = LoggerFactory.getLogger(KubernetesExternalExecutorFactory.class);

    private KubeConfigFactory kubeConfigFactory;
    private PodConfigFactory podConfigFactory;
    private NfsDetailsFactory nfsDetailsFactory;

    private KubernetesExternalExecutorFactory()
    {

    }

    public KubernetesExternalExecutorFactory(KubeConfigFactory kubeConfigFactory, NfsDetailsFactory nfsDetailsFactory)
    {
        this.kubeConfigFactory = kubeConfigFactory;
        this.nfsDetailsFactory = nfsDetailsFactory;
        this.podConfigFactory = new PodConfigFactoryImpl();
    }

    @Override
    public BaseExternalExecutor getExternalExecutor(LaunchDataWrapper launchDataWrapper, String[] commandLineArgs)
    {
        KubeConfig kubeConfig = kubeConfigFactory.createKubeConfig();
        podConfigFactory = new PodConfigFactoryImpl(launchDataWrapper.getPropertyRetriever());
        PodConfig podConfig = podConfigFactory.createPodConfig();
        podConfig.setArguments(commandLineArgs);
        NfsDetails nfsDetails = nfsDetailsFactory.createNfsDetails(NFS_PROPERTIES_PREFIX);
        if(nfsDetails != null)
        {
            podConfig.setNfsSettings(Collections.singletonList(nfsDetails));
        }

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix())
                .setCpuRequestModulator(new CpuRequestModulator()
                {
                    @Override
                    public String getCpuRequest()
                    {
                        return podConfig.getCpuMinRequestCount();
                    }

                    @Override
                    public String getCpuLimit()
                    {
                        return podConfig.getCpuMaxRequestCount();
                    }
                });

        return new KubernetesExternalExecutor(kubeConfig, podConfig, executionConfig);
    }

    public KubeConfigFactory getKubeConfigFactory()
    {
        return kubeConfigFactory;
    }

    public KubernetesExternalExecutorFactory setPodConfigFactory(PodConfigFactory podConfigFactory)
    {
        this.podConfigFactory = podConfigFactory;
        return this;
    }

    public KubernetesExternalExecutorFactory setKubeConfigFactory(KubeConfigFactory kubeConfigFactory)
    {
        this.kubeConfigFactory = kubeConfigFactory;
        return this;
    }

    public KubernetesExternalExecutorFactory setNfsDetailsFactory(NfsDetailsFactory nfsDetailsFactory)
    {
        this.nfsDetailsFactory = nfsDetailsFactory;
        return this;
    }
}