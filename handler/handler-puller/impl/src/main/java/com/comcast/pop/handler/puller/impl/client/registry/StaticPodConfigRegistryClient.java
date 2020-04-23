package com.comcast.pop.handler.puller.impl.client.registry;

import com.comcast.pop.handler.kubernetes.support.config.PodConfigFactory;
import com.comcast.pop.handler.kubernetes.support.config.PodConfigFactoryImpl;
import com.comcast.pop.handler.kubernetes.support.podconfig.client.registry.PodConfigRegistryClient;
import com.comcast.pop.modules.kube.client.config.ConfigMapDetails;
import com.comcast.pop.modules.kube.client.config.KeyPathPair;
import com.comcast.pop.modules.kube.client.config.PodConfig;

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
        podConfigMap.put("executor",
            podConfigFactory.createPodConfig()
                .setServiceAccountName("pop-service")
                .setMemoryRequestCount("1000m")
                .setCpuMinRequestCount("1000m")
                .setCpuMaxRequestCount("1000m")
                .setPodScheduledTimeoutMs(600000L)
                .setReapCompletedPods(true)
                .setPullAlways(false) // for now
                .setImageName("fhexec:1.0.0")
                .setNamePrefix("pop-exec")
                .setConfigMapSettings(Collections.singletonList(
                    new ConfigMapDetails()
                        .setConfigMapName("pop-executor-01")
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