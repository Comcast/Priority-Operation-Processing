package com.theplatform.dfh.cp.modules.kube.fabric8.client.factory;

import com.theplatform.dfh.cp.modules.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.ConnectionTracker;

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

    public PodPushClient getClient(KubeConfig kubeConfig)
    {
        return getClient(kubeConfig, null);
    }

    abstract public PodPushClient getClient(KubeConfig kubeConfig, ConnectionTracker connectionTracker);
}
