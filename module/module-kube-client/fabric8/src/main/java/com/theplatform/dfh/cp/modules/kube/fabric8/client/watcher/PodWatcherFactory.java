package com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher;

import com.theplatform.dfh.cp.modules.kube.client.LogLineAccumulator;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.facade.KubernetesClientFacade;

import java.util.concurrent.CountDownLatch;

/**
 * Basic factory for creating a PodWatcher
 */
public class PodWatcherFactory
{
    public PodWatcherImpl createPodWatcher(KubernetesClientFacade logKubernetesClientFacade, CountDownLatch podScheduled, CountDownLatch podFinishedSuccessOrFailure,
        String kubeNamespace, String podName, LogLineAccumulator logLineAccumulator)
    {
        PodWatcherImpl podWatcherImpl = new PodWatcherImpl();
        podWatcherImpl.setPodName(podName);
        podWatcherImpl.setKubeNamespace(kubeNamespace);
        podWatcherImpl.setScheduledLatch(podScheduled);
        podWatcherImpl.setFinishedLatch(podFinishedSuccessOrFailure);
        podWatcherImpl.setLogLineAccumulator(logLineAccumulator);
        podWatcherImpl.setLogKubernetesClientFacade(logKubernetesClientFacade);
        return podWatcherImpl;
    }
}
