package com.theplatform.dfh.cp.modules.kube.fabric8.client.facade;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
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
    PodResource<Pod, DoneablePod> getPodResource(String nameSpace, String fullName);
    void close();
}
