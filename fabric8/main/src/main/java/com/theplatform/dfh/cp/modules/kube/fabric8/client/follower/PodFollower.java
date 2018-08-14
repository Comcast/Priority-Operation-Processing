package com.theplatform.dfh.cp.modules.kube.fabric8.client.follower;

import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.client.logging.LogLineObserver;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;

/**
 * Convenient tool for starting off a pod and following it until it has completed.
 *
 * A PodFollower is capable of following multiple pods, one per invocation of "startAndFollowPod(...)"
 * The only state kept at object-instance level is immutable and used for all pod executions.
 */
public interface PodFollower<C extends PodPushClient>
{
    /**
     * Synchronous "blocking" call.  You may call this multiple times (in different threads) and that is safe.
     *
     * @param pushBasedPodClient a k8 client wrapped to make push event handling easy.
     * @param podConfig details specific to a particular od
     * @param executionConfig details specific to a particular execution
     * @param logLineObserver a logLine observer that will be attached to the stdout stream of the pod started.
     * @return the LastPhase (success or failure) from the pod being followed.
     */
    public FinalPodPhaseInfo startAndFollowPod(C pushBasedPodClient, PodConfig podConfig, ExecutionConfig executionConfig,
        LogLineObserver logLineObserver);

    public LogLineObserver getDefaultLogLineObserver(ExecutionConfig executionConfig);
}