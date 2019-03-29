package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

public class PodConfigFactoryImpl implements PodConfigFactory
{
    @Override
    public PodConfig createPodConfig()
    {
        return new PodConfig().applyDefaults();
    }

    /**
     * Returns the result of the createPodConfig() call
     * @param templateName Ignored
     * @return
     */
    @Override
    public PodConfig createPodConfig(String templateName)
    {
        return createPodConfig();
    }
}
