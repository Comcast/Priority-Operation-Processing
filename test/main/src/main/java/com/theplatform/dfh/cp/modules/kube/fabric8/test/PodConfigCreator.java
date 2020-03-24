package com.theplatform.dfh.cp.modules.kube.fabric8.test;

import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.test.factory.ConfigFactory;

public interface PodConfigCreator
{
    PodConfig createPodConfig(ConfigFactory configFactory);
}
