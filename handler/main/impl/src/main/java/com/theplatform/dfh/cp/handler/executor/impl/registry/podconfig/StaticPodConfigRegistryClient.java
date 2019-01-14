package com.theplatform.dfh.cp.handler.executor.impl.registry.podconfig;

import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.modules.kube.client.config.ConfigMapDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.podconfig.registry.client.api.PodConfigRegistryClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Hard coded PodConfigs until we have a real registry
 */
public class StaticPodConfigRegistryClient implements PodConfigRegistryClient
{
    private static final String DFH_SERVICE_ACCOUNT_NAME = "ffmpeg-service";
    private static Map<String, PodConfig> podConfigMap = new HashMap<>();

    static
    {
        podConfigMap.put("sample",
                makeDfhBasePod("lab-main-t-aor-fhsamp-t01")
                        .setImageName("docker-lab.repo.theplatform.com/fhsamp:1.0.0")
                        .setNamePrefix("dfh-samp")
        );

        podConfigMap.put("analysis",
                makeDfhBasePod("lab-main-t-aor-fhami-t01")
                        .setImageName("docker-lab.repo.theplatform.com/fhami:1.0.0")
                        .setNamePrefix("dfh-analysis")
        );

        podConfigMap.put("encode",
                makeDfhBasePod("lab-main-t-aor-fheff-t01")
                        .setImageName("docker-lab.repo.theplatform.com/fheff:1.0.0")
                        .setNamePrefix("dfh-encode")
        );

        podConfigMap.put("thumbnail",
                makeDfhBasePod("lab-main-t-aor-fhtff-t01")
                        .setImageName("docker-lab.repo.theplatform.com/fhtff:1.0.0")
                        .setNamePrefix("dfh-thumbnail")
        );

        podConfigMap.put("package",
                makeDfhBasePod("lab-main-t-aor-fhpkm-t01")
                .setImageName("docker-lab.repo.theplatform.com/fhpkm:1.0.0")
                .setNamePrefix("dfh-package")
                .setReapCompletedPods(false) // for debugging
        );

        podConfigMap.get("package").getEnvVars().put("TEST_ENV_VAR","Default test env var");
    }

    @Override
    public PodConfig getPodConfig(String type)
    {
        return podConfigMap.get(type);
    }

    public static PodConfig makeDfhBasePod(String configMapName)
    {
        ConfigMapDetails configMapDetails = new ConfigMapDetails()
                .setConfigMapName(configMapName)
                .setMapKey("external-properties")
                .setMapPath("external.properties")
                .setVolumeName("config-volume")
                .setVolumeMountPath("/config");
        return new PodConfig()
                .setServiceAccountName(DFH_SERVICE_ACCOUNT_NAME)
                .setMemoryRequestCount("1000m")
                .setCpuMinRequestCount("1000m")
                .setCpuMaxRequestCount("1000m")
                .setPodScheduledTimeoutMs(600000L)
                .setReapCompletedPods(true)
                .setPullAlways(true)
                .setEndOfLogIdentifier(BaseHandlerEntryPoint.DFH_POD_TERMINATION_STRING)
                .setConfigMapDetails(configMapDetails);
    }
}
