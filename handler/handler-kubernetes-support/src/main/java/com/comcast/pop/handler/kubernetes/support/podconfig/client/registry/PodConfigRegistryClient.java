package com.comcast.pop.handler.kubernetes.support.podconfig.client.registry;

import com.comcast.pop.handler.kubernetes.support.podconfig.client.registry.api.PodConfigRegistryClientException;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

public interface PodConfigRegistryClient {
    PodConfig getPodConfig(String configMapName) throws PodConfigRegistryClientException;
}
