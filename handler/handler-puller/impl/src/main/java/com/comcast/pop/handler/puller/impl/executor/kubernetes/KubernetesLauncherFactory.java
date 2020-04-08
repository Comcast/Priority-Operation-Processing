package com.comcast.pop.handler.puller.impl.executor.kubernetes;

import com.comcast.pop.handler.puller.impl.client.registry.StaticPodConfigRegistryClient;
import com.comcast.pop.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.comcast.pop.handler.puller.impl.context.PullerContext;
import com.comcast.pop.handler.puller.impl.exception.PullerException;
import com.comcast.pop.handler.kubernetes.support.config.KubeConfigFactory;
import com.comcast.pop.handler.kubernetes.support.podconfig.client.registry.JsonPodConfigRegistryClient;
import com.comcast.pop.handler.kubernetes.support.podconfig.client.registry.PodConfigRegistryClient;
import com.comcast.pop.handler.kubernetes.support.podconfig.client.registry.api.PodConfigRegistryClientException;
import com.comcast.pop.handler.puller.impl.executor.BaseLauncher;
import com.comcast.pop.handler.puller.impl.executor.LauncherFactory;

import com.comcast.pop.modules.kube.client.CpuRequestModulator;
import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for producing executors to get MediaProperties (via a mediainfo launch through kubernetes)
 */
public class KubernetesLauncherFactory implements LauncherFactory
{
    private static final String DEFAULT_JSON_REGISTRY_PATH = "/app/config/registry.json";
    private static final Object registryLock = new Object();
    private static Logger logger = LoggerFactory.getLogger(KubernetesLauncherFactory.class);

    private PullerLaunchDataWrapper launchDataWrapper;
    private int podRetryDelay = 2000;

    private static PodConfigRegistryClient podConfigRegistryClient;
    private KubeConfigFactory kubeConfigFactory;

    private static final String EXEC_OPERATION_TYPE = "executor";

    public KubernetesLauncherFactory(PullerLaunchDataWrapper pullerLaunchDataWrapper)
    {
        synchronized (registryLock)
        {
            // no point in reloading the client over and over
            if (podConfigRegistryClient == null)
            {
                boolean useStaticRegistryClient = Boolean.parseBoolean(pullerLaunchDataWrapper.getPropertyRetriever().getField("useStaticRegistryClient", "false"));
                String jsonRegistryPath = pullerLaunchDataWrapper.getPropertyRetriever().getField("registryPathOverride", DEFAULT_JSON_REGISTRY_PATH);

                if (useStaticRegistryClient)
                {
                    podConfigRegistryClient = new StaticPodConfigRegistryClient();
                }
                else
                {
                    podConfigRegistryClient = new JsonPodConfigRegistryClient(jsonRegistryPath);
                }
            }
        }
        this.launchDataWrapper = pullerLaunchDataWrapper;
    }

    @Override
    public BaseLauncher createLauncher(PullerContext pullerContext)
    {
        // This is a call to the local K8s API. We can't use
        // any proxies. Make sure that we are not.
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");

        KubeConfig kubeConfig = kubeConfigFactory.createKubeConfig();

        PodConfig executorPodConfig = null;
        try {
            executorPodConfig = podConfigRegistryClient.getPodConfig(EXEC_OPERATION_TYPE);
        } catch (PodConfigRegistryClientException e) {
            logger.error("There was a problem trying to retrieve the PodConfig from PodConfigRegistryClient.");
        }

        if (executorPodConfig != null)
        {
            return buildLauncher(executorPodConfig, kubeConfig);
        }
        else
        {
            logger.error("Could not retrieve PodConfig from Registry..");
            throw new PullerException("Could not retrieve `" + EXEC_OPERATION_TYPE + "` handler from PodConfig registry..");
        }
    }

    private BaseLauncher buildLauncher(PodConfig podConfig, KubeConfig kubeConfig)
    {
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

        return new KubernetesLauncher(kubeConfig, podConfig, executionConfig, launchDataWrapper);
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