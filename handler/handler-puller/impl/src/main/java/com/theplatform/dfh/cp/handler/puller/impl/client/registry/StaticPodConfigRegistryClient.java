package com.theplatform.dfh.cp.handler.puller.impl.client.registry;

import com.theplatform.dfh.cp.handler.kubernetes.support.config.PodConfigFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.PodConfigFactoryImpl;
import com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry.PodConfigRegistryClient;
import com.theplatform.dfh.cp.modules.kube.client.config.ConfigMapDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.KeyPathPair;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

import java.util.Arrays;
import java.util.Collections;
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
                .setServiceAccountName("fission-service")
                .setMemoryRequestCount("1000m")
                .setCpuMinRequestCount("1000m")
                .setCpuMaxRequestCount("1000m")
                .setPodScheduledTimeoutMs(600000L)
                .setReapCompletedPods(true)
                .setPullAlways(true) // for now
                .setImageName("docker-lab.repo.theplatform.com/fhsamp:1.0.1")
                .setNamePrefix("fission-samp")
        );
        podConfigMap.put("executor",
            podConfigFactory.createPodConfig()
                .setServiceAccountName("fission-service")
                .setMemoryRequestCount("1000m")
                .setCpuMinRequestCount("1000m")
                .setCpuMaxRequestCount("1000m")
                .setPodScheduledTimeoutMs(600000L)
                .setReapCompletedPods(true)
                .setPullAlways(true) // for now
                .setImageName("docker-lab.repo.theplatform.com/fhexec:1.0.2")
                .setNamePrefix("fission-exec")
                .setConfigMapSettings(Collections.singletonList(
                    new ConfigMapDetails()
                        .setConfigMapName("lab-main-t-aor-fhexec-t02")
                        .setVolumeName("config-volume")
                        .setVolumeMountPath("/app/config")
                        .setMapKeyPaths(
                            Arrays.asList(
                                new KeyPathPair("external-properties", "external.properties"),
                                new KeyPathPair("registry-json", "registry.json")
                            )
                        )
                    )
                )
        );
    }

    @Override
    public PodConfig getPodConfig(String type)
    {
        return podConfigMap.get(type);
    }
}