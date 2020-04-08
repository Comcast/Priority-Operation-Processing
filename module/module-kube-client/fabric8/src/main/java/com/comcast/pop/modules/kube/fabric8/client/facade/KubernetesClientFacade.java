package com.comcast.pop.modules.kube.fabric8.client.facade;

import com.comcast.pop.modules.kube.fabric8.client.watcher.ConnectionTracker;
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
    /**
     * Starts the specified pod
     * @param podToCreate The pod to request creation of
     * @return The created pod
     */
    Pod startPod(Pod podToCreate);

    /**
     * Updates the pod annotations of the specified pod
     * @param podName The name of the pod to update
     * @param annotations The annotations to specify on the pod
     */
    void updatePodAnnotations(String podName, Map<String, String> annotations);

    /**
     * Retrieves the pod annoations on the specified pod
     * @param podName The name of the pod to retrieve the annotations from
     * @return The map of annotations
     */
    Map<String, String> getPodAnnotations(String podName);

    /**
     * Gets the PodResource object associated with the namespace and pod
     * @param namespace The namespace of the pod to query
     * @param podName The name of the pod to query
     * @return
     */
    PodResource<Pod, DoneablePod> getPodResource(String namespace, String podName);

    /**
     * Gets the UTC timestamp of the last log line from the pod
     * @param namespace The namespace of the pod to query
     * @param podName The name of the pod to query
     * @return The epoch time of the last log line or null if it could not be determined
     */
    Long getLastLogLineTimestamp(String namespace, String podName);

    /**
     * Closes the underlying kubernetes client
     */
    void close();

    /**
     * Retrieves the underlying kubernetes client
     * @return The underlying client
     */
    KubernetesClient getInternalClient();

    /**
     * Retrieves the ConnectionTracker associated with this client (tracks all socket creates)
     * @return a ConnectionTracker
     */
    ConnectionTracker getConnectionTracker();

    /**
     * Gets the name of this facade
     * @return The name of the facade
     */
    String getName();

    /**
     * Sets the name of the facade
     * @param name The name to set on the facade
     */
    void setName(String name);
}
