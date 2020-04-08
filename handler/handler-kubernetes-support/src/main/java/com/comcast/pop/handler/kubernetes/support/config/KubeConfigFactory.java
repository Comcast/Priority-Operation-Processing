package com.comcast.pop.handler.kubernetes.support.config;

import com.comcast.pop.modules.kube.client.config.KubeConfig;

public interface KubeConfigFactory
{
    KubeConfig createKubeConfig();
}
