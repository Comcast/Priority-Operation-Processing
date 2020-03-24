package com.theplatform.dfh.cp.modules.kube.fabric8.client.facade;

public class RetryablePodResourceFacadeFactory implements PodResourceFacadeFactory
{
    public RetryablePodResource create(KubernetesClientFacade kubernetesClientFacade, String namespace, String podName)
    {
        return new RetryablePodResource(kubernetesClientFacade.getPodResource(namespace, podName));
    }
}
