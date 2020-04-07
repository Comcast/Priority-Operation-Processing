package com.comcast.pop.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

public interface PodConfigFactory
{
    /**
     * Creates a basic PodConfig
     * @return new PodConfig
     */
    PodConfig createPodConfig();

    /**
     * Creates a PodConfig based on the specified templateName
     * @return new PodConfig (implementations may vary)
     */
    PodConfig createPodConfig(String templateName);
}
