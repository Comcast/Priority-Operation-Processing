package com.theplatform.dfh.cp.handler.executor.impl.podconfig.registry;

import com.theplatform.dfh.cp.modules.kube.client.config.ConfigMapDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.NfsDetails;
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
                .setReapCompletedPods(true)
                .setPullAlways(true) // for now
                .setImageName("docker-lab.repo.theplatform.com/fhsamp:1.0.0")
                .setNamePrefix("dfh-samp")
                .setEndOfLogIdentifier("SampleComplete")
                .setConfigMapDetails(new ConfigMapDetails()
                    .setConfigMapName("lab-main-t-aor-fhsamp-t01")
                    .setMapKey("external-properties")
                    .setMapPath("external.properties")
                    .setVolumeName("config-volume")
                    .setVolumeMountPath("/config"))
        );

        podConfigMap.put("encode",
            new PodConfig()
                .setServiceAccountName("ffmpeg-service")
                .setMemoryRequestCount("1000m")
                .setCpuMinRequestCount("1000m")
                .setCpuMaxRequestCount("1000m")
                .setPodScheduledTimeoutMs(600000L)
                .setReapCompletedPods(true)
                .setPullAlways(true) // for now
                .setImageName("docker-lab.repo.theplatform.com/fheff:1.0.0")
                .setNamePrefix("dfh-encode")
                .setEndOfLogIdentifier("ffmpeg_handler_end")
                .setConfigMapDetails(new ConfigMapDetails()
                    .setConfigMapName("lab-main-t-aor-fheff-t01")
                    .setMapKey("external-properties")
                    .setMapPath("external.properties")
                    .setVolumeName("config-volume")
                    .setVolumeMountPath("/config"))
        );

        // minikube specific encode handler
        podConfigMap.put("encodeMinikube",
            new PodConfig()
                .setServiceAccountName("ffmpeg-service")
                .setMemoryRequestCount("1000m")
                .setCpuMinRequestCount("1000m")
                .setCpuMaxRequestCount("1000m")
                .setPodScheduledTimeoutMs(600000L)
                .setReapCompletedPods(true)
                .setPullAlways(true) // for now
                .setImageName("docker-lab.repo.theplatform.com/fheff:1.0.0")
                .setNamePrefix("dfh-encode")
                .setEndOfLogIdentifier("ffmpeg_handler_end")
                .setConfigMapDetails(new ConfigMapDetails()
                    .setConfigMapName("lab-main-t-aor-fheff-t01")
                    .setMapKey("external-properties")
                    .setMapPath("external.properties")
                    .setVolumeName("config-volume")
                    .setVolumeMountPath("/config"))
                .setNfsDetails(new NfsDetails()
                    .setNfsServerPath("/")
                    .setNfsServer("192.168.99.1")
                    .setNfsMountPaths(new String[] {"/mnt"})
                    .setNfsReadOnly(false))
        );
    }

    @Override
    public PodConfig getPodConfig(String type)
    {
        return podConfigMap.get(type);
    }
}
