package com.comcast.pop.modules.kube.fabric8.client.follower;

import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.fabric8.client.PodPushClient;
import com.comcast.pop.modules.kube.client.logging.LogLineObserver;
import com.comcast.pop.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;
import com.comcast.pop.modules.kube.fabric8.client.watcher.PodEventListener;

import java.util.Map;

/**
 * Convenient tool for starting off a pod and following it until it has completed.
 */
public interface PodFollower<C extends PodPushClient>
{
    /**
     * Synchronous "blocking" call.
     * @param logLineObserver a logLine observer that will be attached to the stdout stream of the pod started.
     * @return the LastPhase (success or failure) from the pod being followed.
     */
    public FinalPodPhaseInfo startAndFollowPod(LogLineObserver logLineObserver);

    public LogLineObserver getDefaultLogLineObserver(ExecutionConfig executionConfig);

    public PodPushClient getPodPushClient();

    public Map<String, String> getPodAnnotations();

    public PodFollower addEventListener(PodEventListener listener);
}