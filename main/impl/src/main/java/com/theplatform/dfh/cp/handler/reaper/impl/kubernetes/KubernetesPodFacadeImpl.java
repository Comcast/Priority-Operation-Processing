package com.theplatform.dfh.cp.handler.reaper.impl.kubernetes;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.List;
import java.util.Map;

public class KubernetesPodFacadeImpl implements KubernetesPodFacade
{
    private final KubernetesClient kubernetesClient;

    public KubernetesPodFacadeImpl(KubernetesClient kubernetesClient)
    {
        this.kubernetesClient = kubernetesClient;
    }

    @Override
    public List<Pod> lookupPods(String namespace, Map<String, String> fields)
    {
        return kubernetesClient.pods().inNamespace(namespace).withFields(fields).list().getItems();
    }

    @Override
    public Boolean deletePods(List<Pod> podsToDelete)
    {
        return kubernetesClient.pods().delete(podsToDelete);
    }
}
