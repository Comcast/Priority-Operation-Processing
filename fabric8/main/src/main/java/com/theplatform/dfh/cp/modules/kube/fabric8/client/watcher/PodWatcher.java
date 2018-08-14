package com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Watcher;

/**
 * This interface is created once per pod execution.
 * It's state is mutated as needed during the execution of the pod.
 */
public interface PodWatcher extends Watcher<Pod>
{
    /**
     * Gets a "LastPhase", which could be null if the pod hasn't finished.
     * @return success/failure (null if RUNNING/PENDING/etc)
     */
    public FinalPodPhaseInfo getFinalPodPhaseInfo();

    /**
     * You may reset logging at anytime, if you feel the log watching has failed you.
     */
    void resetLogging();
}
