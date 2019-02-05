package com.theplatform.dfh.cp.handler.base.podconfig.registry.client;

import com.theplatform.dfh.cp.handler.base.podconfig.registry.client.api.PodConfigRegistryClientException;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

public interface PodConfigRegistryClient {
    PodConfig getPodConfig(String configMapName) throws PodConfigRegistryClientException;
}
