package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;

public interface KubeConfigFactory
{
    KubeConfig createKubeConfig();
}
