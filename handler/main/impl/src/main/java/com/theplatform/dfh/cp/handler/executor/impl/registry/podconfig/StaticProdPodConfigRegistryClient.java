package com.theplatform.dfh.cp.handler.executor.impl.registry.podconfig;

import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.PodConfigFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.PodConfigFactoryImpl;
import com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry.PodConfigRegistryClient;
import com.theplatform.dfh.cp.modules.kube.client.config.ConfigMapDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.KeyPathPair;
import com.theplatform.dfh.cp.modules.kube.client.config.NfsDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Hard coded PodConfigs until we have a real registry
 */
public class StaticProdPodConfigRegistryClient implements PodConfigRegistryClient
{
    private static final String DFH_SERVICE_ACCOUNT_NAME = "dfh-service";
    private static PodConfigFactory podConfigFactory = new PodConfigFactoryImpl();
    private static Map<String, PodConfig> podConfigMap = new HashMap<>();

    static
    {
        podConfigMap.put("sample",
                makeDfhBasePod("prod-us-a-aor-fhsamp-p01")
                        .setImageName("docker-lab.repo.theplatform.com/fhsamp:1.0.2")
                        .setNamePrefix("dfh-samp")
        );

        podConfigMap.put("analysis",
                makeDfhBasePod("prod-us-a-aor-fhami-p01")
                        .setImageName("docker-prod.repo.theplatform.com/fhami:1.0.1")
                        .setNamePrefix("dfh-analysis")
        );

        podConfigMap.put("encode",
                makeDfhBasePod("prod-us-a-aor-fheff-p01")
                        .setImageName("docker-prod.repo.theplatform.com/fheff:1.0.1")
                        .setNamePrefix("dfh-encode")
        );

        podConfigMap.put("thumbnail",
                makeDfhBasePod("prod-us-a-aor-fhtff-p01")
                        .setImageName("docker-lab.repo.theplatform.com/fhtff:1.0.1")
                        .setNamePrefix("dfh-thumbnail")
        );

        podConfigMap.put("filmstrip",
            makeDfhBasePod("prod-us-a-aor-fhfsff-p01")
                .setImageName("docker-lab.repo.theplatform.com/fhfsff:1.0.1")
                .setNamePrefix("dfh-filmstrip")
        );

        podConfigMap.put("package",
                makeDfhBasePod("prod-us-a-aor-fhpkm-p01")
                .setImageName("docker-prod.repo.theplatform.com/fhpkm:1.0.4")
                .setNamePrefix("dfh-package")
                .setNfsSettings(Collections.singletonList(new NfsDetails()
                    .setNfsServer("fs-71d2e4da.efs.us-west-2.amazonaws.com")
                    .setNfsServerPath("/")
                    .setNfsReadOnly(false)
                    .setNfsMountPaths(new String[] {"/media_symlib","/media_symarc"})))
        );

        podConfigMap.put("telephone",
            makeDfhBasePod("lab-main-t-aor-fhtele-t01")
                .setImageName("docker-proto.repo.theplatform.com/fhtele:1.0.0")
                .setNamePrefix("dfh-telephone")
        );

        podConfigMap.put("delete",
            makeDfhBasePod("prod-us-a-aor-fhdel-p01")
                .setImageName("docker-lab.repo.theplatform.com/fhdel:1.0.0")
                .setNamePrefix("dfh-fission-delete")
        );
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
                .setMapKeyPaths(Arrays.asList(
                    new KeyPathPair("env-properties", "env.properties"),
                    new KeyPathPair("external-properties", "external.properties")
                ))
                .setVolumeName("config-volume")
                .setVolumeMountPath("/app/config");
        return podConfigFactory.createPodConfig()
                .setServiceAccountName(DFH_SERVICE_ACCOUNT_NAME)
                .setMemoryRequestCount("1000m")
                .setCpuMinRequestCount("1000m")
                .setCpuMaxRequestCount("1000m")
                .setPodScheduledTimeoutMs(600000L)
                .setReapCompletedPods(true)
                .setPullAlways(true)
                .setEndOfLogIdentifier(BaseHandlerEntryPoint.DFH_POD_TERMINATION_STRING)
                .setConfigMapSettings(Collections.singletonList(configMapDetails));
    }
}
