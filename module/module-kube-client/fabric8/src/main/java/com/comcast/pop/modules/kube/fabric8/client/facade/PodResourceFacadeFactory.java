package com.comcast.pop.modules.kube.fabric8.client.facade;

public interface PodResourceFacadeFactory
{
    PodResourceFacade create(KubernetesClientFacade kubernetesClientFacade, String namespace, String podName);
}
