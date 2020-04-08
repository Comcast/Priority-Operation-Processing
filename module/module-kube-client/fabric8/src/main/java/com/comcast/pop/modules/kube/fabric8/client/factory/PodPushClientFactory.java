package com.comcast.pop.modules.kube.fabric8.client.factory;

import com.comcast.pop.modules.kube.client.CpuRequestModulator;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.fabric8.client.PodPushClient;

abstract public class PodPushClientFactory<C extends CpuRequestModulator>
{
    protected KubeConfig kubeConfig;

    public KubeConfig getKubeConfig()
    {
        return kubeConfig;
    }

    public void setKubeConfig(KubeConfig kubeConfig)
    {
        this.kubeConfig = kubeConfig;
    }

    abstract public PodPushClient getClient(KubeConfig kubeConfig);
}
