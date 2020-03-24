package com.theplatform.dfh.cp.modules.kube.fabric8.test.factory;

import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

public interface ConfigFactory
{
    KubeConfig getDefaultKubeConfig();
    PodConfig getDefaultPodConfig();
}