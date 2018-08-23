package com.theplatform.dfh.cp.handler.executor.impl.podconfig.registry;

import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.podconfig.registry.client.api.PodConfigRegistryClient;

import java.util.HashMap;
import java.util.Map;

public class StaticPodConfigRegistryClient implements PodConfigRegistryClient
{
    private static Map<String, PodConfig> podConfigMap = new HashMap<>();

    static
    {
        podConfigMap.put("sample",
            new PodConfig()
                .setServiceAccountName("ffmpeg-service")
                .setMemoryRequestCount("1000m")
                .setCpuMinRequestCount("1000m")
                .setCpuMaxRequestCount("1000m")
                .setPodScheduledTimeoutMs(600000L)
                .setReapCompletedPods(false)
                .setPullAlways(true) // for now
                .setImageName("docker-lab.repo.theplatform.com/fhsamp:1.0.0")
                .setNamePrefix("dfh-samp")
                .setEndOfLogIdentifier("SampleComplete")
        );
    }

    @Override
    public PodConfig getPodConfig(String type)
    {
        return podConfigMap.get(type);
    }
}
