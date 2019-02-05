package com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes;

import com.theplatform.dfh.cp.handler.base.podconfig.registry.client.JsonPodConfigRegistryClient;
import com.theplatform.dfh.cp.handler.base.podconfig.registry.client.PodConfigRegistryClient;
import com.theplatform.dfh.cp.handler.base.podconfig.registry.client.api.PodConfigRegistryClientException;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactory;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContext;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.cp.handler.puller.impl.executor.LauncherFactory;

import com.theplatform.dfh.cp.modules.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
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

    private static final String EXEC_OPERATION_TYPE = "executor";

    public KubernetesLauncherFactory()
    {
        this.podConfigRegistryClient = new JsonPodConfigRegistryClient("/config/registry.json");
    }

    @Override
    public BaseLauncher createLauncher(PullerContext pullerContext)
    {
        KubeConfig kubeConfig = kubeConfigFactory.createKubeConfig();

        PodConfig podConfig = null;
        try {
            podConfig = podConfigRegistryClient.getPodConfig(EXEC_OPERATION_TYPE);
        } catch (PodConfigRegistryClientException e) {
            logger.error("There was a problem trying to retrieve the PodConfig from PodConfigRegistryClient.");
        }

        if (podConfig != null)
        {
            return buildLauncher(podConfig, pullerContext, kubeConfig);
        }
        else
        {
            logger.error("Could not retrieve PodConfig from Registry..");
            return null;
        }
    }

    private BaseLauncher buildLauncher(PodConfig podConfig, PullerContext pullerContext, KubeConfig kubeConfig)
    {
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