package com.theplatform.dfh.cp.handler.executor.impl.registry.podconfig;

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
    private static final String FISSION_SERVICE_ACCOUNT_NAME = "fission-service";
    private static PodConfigFactory podConfigFactory = new PodConfigFactoryImpl();
    private static Map<String, PodConfig> podConfigMap = new HashMap<>();
    private static JsonHelper jsonHelper = new JsonHelper();

    static
    {
        podConfigMap.put("sample",
                makeBasePod("fission-sample-01")
                        .setImageName("fission-sample:1.0.2")
                        .setNamePrefix("fission-sample")
                        .setPullAlways(false)
                        .setReapCompletedPods(false)
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

    public static PodConfig makeBasePod(String configMapName)
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
                .setServiceAccountName(FISSION_SERVICE_ACCOUNT_NAME)
                .setMemoryRequestCount("1000m")
                .setCpuMinRequestCount("1000m")
                .setCpuMaxRequestCount("1000m")
                .setPodScheduledTimeoutMs(600000L)
                .setReapCompletedPods(true)
                .setPullAlways(true)
                .setEndOfLogIdentifier(BaseHandlerEntryPoint.POD_TERMINATION_STRING)
                .setConfigMapSettings(Collections.singletonList(configMapDetails));
    }
}
