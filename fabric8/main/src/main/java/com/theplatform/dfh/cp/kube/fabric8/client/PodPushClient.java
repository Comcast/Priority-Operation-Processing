package com.theplatform.dfh.cp.kube.fabric8.client;

import com.theplatform.dfh.cp.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.kube.fabric8.client.watcher.PodWatcher;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Client used to start up a Kubernetes Pod.  Instances should be shared across multiple Pods.
 */
public interface PodPushClient
{
    public KubeConfig getKubeConfig();

    public void setKubeConfig(KubeConfig kubeConfig);

    public void setFabric8Client(DefaultKubernetesClient fabric8Client);

    public DefaultKubernetesClient getFabric8Client();

    public void close();

    public boolean deletePod(String podName);

    public PodWatcher start(PodConfig podConfig, ExecutionConfig executionConfig,
        CountDownLatch podScheduled, CountDownLatch podFinishedSuccessOrFailure);

    public void startWithoutWatcher(PodConfig podConfig, ExecutionConfig executionConfig);

    /**
     * Edit annotations by overwriting any intersecting key names, but preserving non-intersecting key names.
     *
     * @param podName name of pod to load existing annotations from and union with passed in annotations.
     * @param annotations new annotations to apply to existing (with precedence given to new).
     */
    public void editPodAnnotations(String podName, Map<String, String> annotations);
}