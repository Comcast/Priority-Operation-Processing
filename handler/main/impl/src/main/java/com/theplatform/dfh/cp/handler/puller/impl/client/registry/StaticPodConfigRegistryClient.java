package com.theplatform.dfh.cp.handler.puller.impl.client.registry;

import com.theplatform.dfh.cp.handler.kubernetes.support.config.PodConfigFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.PodConfigFactoryImpl;
import com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry.PodConfigRegistryClient;
import com.theplatform.dfh.cp.modules.kube.client.config.ConfigMapDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

import java.util.HashMap;
import java.util.Map;

public class StaticPodConfigRegistryClient implements PodConfigRegistryClient
{
    private static Map<String, PodConfig> podConfigMap = new HashMap<>();
    private static PodConfigFactory podConfigFactory = new PodConfigFactoryImpl();

    static
    {
        podConfigMap.put("sample",
            podConfigFactory.createPodConfig()
                .setServiceAccountName("dfh-service")
                .setMemoryRequestCount("1000m")
                .setCpuMinRequestCount("1000m")
                .setCpuMaxRequestCount("1000m")
                .setPodScheduledTimeoutMs(600000L)
                .setReapCompletedPods(true)
                .setPullAlways(true) // for now
                .setImageName("docker-lab.repo.theplatform.com/fhsamp:1.0.1")
                .setNamePrefix("dfh-samp")
        );
        podConfigMap.put("executor",
            podConfigFactory.createPodConfig()
                .setServiceAccountName("dfh-service")
                .setMemoryRequestCount("1000m")
                .setCpuMinRequestCount("1000m")
                .setCpuMaxRequestCount("1000m")
                .setPodScheduledTimeoutMs(600000L)
                .setReapCompletedPods(true)
                .setPullAlways(true) // for now
                .setImageName("docker-lab.repo.theplatform.com/fhexec:1.0.1")
                .setNamePrefix("dfh-exec")
                .setConfigMapDetails(new ConfigMapDetails()
                    .setConfigMapName("lab-main-t-aor-fhexec-t01")
                    .setMapKey("external-properties")
                    .setMapPath("external.properties")
                    .setVolumeName("config-volume")
                    .setVolumeMountPath("/config"))
        );
    }

    @Override
    public PodConfig getPodConfig(String type)
    {
        return podConfigMap.get(type);
    }
}