package com.comcast.pop.modules.kube.fabric8.test;

import com.comcast.pop.modules.kube.client.config.PodConfig;
import com.comcast.pop.modules.kube.fabric8.test.factory.ConfigFactory;

public interface PodConfigCreator
{
    PodConfig createPodConfig(ConfigFactory configFactory);
}
