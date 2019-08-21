package com.theplatform.dfh.cp.modules.kube.fabric8.client.facade;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.ConnectionTracker;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.PodResource;

import java.util.Map;

/**
 * Wrapper for the KubernetesClient functionality
 */
public interface KubernetesClientFacade
{
    Pod startPod(Pod podToCreate);
    void updatePodAnnotations(String podName, Map<String, String> annotations);
    Map<String, String> getPodAnnotations(String podName);
    PodResource<Pod, DoneablePod> getPodResource(String namespace, String podName);
    void close();
    KubernetesClient getInternalClient();
    ConnectionTracker getConnectionTracker();
    String getName();
    void setName(String name);
}
