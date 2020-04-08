package com.comcast.pop.modules.kube.fabric8.client;

import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import com.comcast.pop.modules.kube.fabric8.client.client.KubernetesHttpClients;
import com.comcast.pop.modules.kube.fabric8.client.watcher.PodWatcher;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Client used to start up a Kubernetes Pod.  Instances should be shared across multiple Pods.
 */
public interface PodPushClient
{
    KubeConfig getKubeConfig();

    void setKubeConfig(KubeConfig kubeConfig);

    KubernetesHttpClients getKubernetesHttpClients();

    void setKubernetesHttpClients(KubernetesHttpClients kubernetesHttpClients);

    void close();

    boolean deletePod(String podName);

    PodWatcher start(PodConfig podConfig, ExecutionConfig executionConfig,
        CountDownLatch podScheduled, CountDownLatch podFinishedSuccessOrFailure);

    void startWithoutWatcher(PodConfig podConfig, ExecutionConfig executionConfig);

    /**
     * Edit annotations by overwriting any intersecting key names, but preserving non-intersecting key names.
     *
     * @param podName name of pod to load existing annotations from and union with passed in annotations.
     * @param annotations new annotations to apply to existing (with precedence given to new).
     */
    void editPodAnnotations(String podName, Map<String, String> annotations);
}