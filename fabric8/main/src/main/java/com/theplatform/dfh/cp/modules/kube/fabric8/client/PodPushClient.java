package com.theplatform.dfh.cp.modules.kube.fabric8.client;

import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.facade.KubernetesClientFacade;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodWatcher;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Client used to start up a Kubernetes Pod.  Instances should be shared across multiple Pods.
 */
public interface PodPushClient
{
    KubeConfig getKubeConfig();

    void setKubeConfig(KubeConfig kubeConfig);

    void setKubernetesClient(KubernetesClientFacade kubernetesClient);

    KubernetesClientFacade getKubernetesClient();

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