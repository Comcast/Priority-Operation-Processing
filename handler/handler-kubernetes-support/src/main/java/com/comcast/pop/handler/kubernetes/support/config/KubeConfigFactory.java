package com.comcast.pop.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;

public interface KubeConfigFactory
{
    KubeConfig createKubeConfig();
}
