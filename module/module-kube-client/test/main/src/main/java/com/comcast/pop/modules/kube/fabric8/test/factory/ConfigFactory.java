package com.comcast.pop.modules.kube.fabric8.test.factory;

import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;

public interface ConfigFactory
{
    KubeConfig getDefaultKubeConfig();
    PodConfig getDefaultPodConfig();
}