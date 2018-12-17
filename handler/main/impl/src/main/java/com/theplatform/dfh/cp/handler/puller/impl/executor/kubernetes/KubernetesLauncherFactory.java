package com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes;

import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactory;
import com.theplatform.dfh.cp.handler.puller.impl.client.registry.StaticPodConfigRegistryClient;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContext;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.cp.handler.puller.impl.executor.LauncherFactory;

import com.theplatform.dfh.cp.modules.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.podconfig.registry.client.api.PodConfigRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for producing executors to get MediaProperties (via a mediainfo launch through kubernetes)
 */
public class KubernetesLauncherFactory implements LauncherFactory
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesLauncherFactory.class);

    private int podRetryDelay = 2000;

    private PodConfigRegistryClient podConfigRegistryClient;
    private KubeConfigFactory kubeConfigFactory;

    private static final String EXEC_OPERATION_TYPE = "exec"; // todo different name?  should this be here?

    public KubernetesLauncherFactory()
    {
        this.podConfigRegistryClient = new StaticPodConfigRegistryClient();
    }

    @Override
    public BaseLauncher createLauncher(PullerContext pullerContext)
    {
        KubeConfig kubeConfig = kubeConfigFactory.createKubeConfig();

        PodConfig podConfig = podConfigRegistryClient.getPodConfig(EXEC_OPERATION_TYPE);

        String execConfigMapName = pullerContext.getLaunchDataWrapper().getPullerConfig().getExecConfigMapName();
        if(execConfigMapName != null)
            podConfig.getConfigMapDetails().setConfigMapName(execConfigMapName);

        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix())
            .addEnvVar("LOG_LEVEL", "DEBUG");

        executionConfig.setCpuRequestModulator(new CpuRequestModulator()
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


        return new KubernetesLauncher(kubeConfig, podConfig, executionConfig);
    }

    public PodConfigRegistryClient getPodConfigRegistryClient()
    {
        return podConfigRegistryClient;
    }

    public KubernetesLauncherFactory setPodConfigRegistryClient(PodConfigRegistryClient podConfigRegistryClient)
    {
        this.podConfigRegistryClient = podConfigRegistryClient;
        return this;
    }

    public KubeConfigFactory getKubeConfigFactory()
    {
        return kubeConfigFactory;
    }

    public KubernetesLauncherFactory setKubeConfigFactory(KubeConfigFactory kubeConfigFactory)
    {
        this.kubeConfigFactory = kubeConfigFactory;
        return this;
    }
}