package com.theplatform.dfh.cp.handler.executor.impl.registry.podconfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.PodConfigFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.PodConfigFactoryImpl;
import com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry.PodConfigRegistryClient;
import com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry.api.PodConfigRegistryClientException;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.kube.client.config.ConfigMapDetails;
import com.theplatform.dfh.cp.modules.kube.client.config.KeyPathPair;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Hard coded PodConfigs until we have a real registry
 */
public class StaticPodConfigRegistryClient implements PodConfigRegistryClient
{
    private static final String DFH_SERVICE_ACCOUNT_NAME = "dfh-service";
    private static PodConfigFactory podConfigFactory = new PodConfigFactoryImpl();
    private static Map<String, PodConfig> podConfigMap = new HashMap<>();
    private static JsonHelper jsonHelper = new JsonHelper();

    static
    {
        podConfigMap.put("sample",
                makeDfhBasePod("lab-main-t-aor-fhsamp-t01")
                        .setImageName("docker-lab.repo.theplatform.com/fhsamp:1.0.2")
                        .setNamePrefix("dfh-samp")
        );

        podConfigMap.put("slack",
            makeDfhBasePod("lab-main-t-aor-fhtele-t01")
                .setImageName("docker-proto.repo.theplatform.com/fhslak:1.0.0")
                .setNamePrefix("dfh-fission-slack")
        );

        podConfigMap.put("analysis",
                makeDfhBasePod("lab-main-t-aor-fhami-t01")
                        .setImageName("docker-lab.repo.theplatform.com/fhami:1.0.3")
                        .setNamePrefix("dfh-analysis")
        );

        podConfigMap.put("encode",
                makeDfhBasePod("lab-main-t-aor-fheff-t01")
                        .setImageName("docker-lab.repo.theplatform.com/fheff:1.0.1")
                        .setNamePrefix("dfh-encode")
        );

        podConfigMap.put("thumbnail",
                makeDfhBasePod("lab-main-t-aor-fhtff-t01")
                        .setImageName("docker-lab.repo.theplatform.com/fhtff:1.0.1")
                        .setNamePrefix("dfh-thumbnail")
        );

        podConfigMap.put("filmstrip",
            makeDfhBasePod("lab-main-t-aor-fhfsff-t01")
                .setImageName("docker-lab.repo.theplatform.com/fhfsff:1.0.1")
                .setNamePrefix("dfh-filmstrip")
        );

        podConfigMap.put("package",
                makeDfhBasePod("lab-main-t-aor-fhpkm-t01")
                .setImageName("docker-lab.repo.theplatform.com/fhpkm:1.0.2")
                .setNamePrefix("dfh-package")
        );

        podConfigMap.put("delete",
            makeDfhBasePod("lab-main-t-aor-fhdel-t01")
                .setImageName("docker-lab.repo.theplatform.com/fhdel:1.0.0")
                .setNamePrefix("dfh-delete")
        );

        podConfigMap.put("telephone",
            makeDfhBasePod("lab-main-t-aor-fhtele-t01")
                .setImageName("docker-proto.repo.theplatform.com/fhtele:1.0.0")
                .setNamePrefix("dfh-telephone")
        );
    }

    @Override
    public PodConfig getPodConfig(String type) throws PodConfigRegistryClientException
    {
        PodConfig originalConfig = podConfigMap.get(type);
        ObjectMapper objectMapper = jsonHelper.getObjectMapper();
        IOException ioException = null;
        if (originalConfig != null)
        {
            try
            {
                return objectMapper.readValue(objectMapper.writeValueAsString(originalConfig), PodConfig.class);
            }
            catch(IOException e)
            {
                ioException = e;
            }
        }
        throw new PodConfigRegistryClientException("Failed to get pod config type: " + type, ioException);
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
