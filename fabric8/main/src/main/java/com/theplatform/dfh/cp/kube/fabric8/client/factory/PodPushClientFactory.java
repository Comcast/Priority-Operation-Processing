package com.theplatform.dfh.cp.kube.fabric8.client.factory;

import com.theplatform.dfh.cp.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.kube.fabric8.client.PodPushClient;


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
