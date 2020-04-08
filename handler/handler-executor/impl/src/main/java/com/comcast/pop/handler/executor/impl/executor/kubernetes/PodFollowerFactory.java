package com.comcast.pop.handler.executor.impl.executor.kubernetes;

import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import com.comcast.pop.modules.kube.fabric8.client.PodPushClient;
import com.comcast.pop.modules.kube.fabric8.client.follower.PodFollower;
import com.comcast.pop.modules.kube.fabric8.client.follower.PodFollowerImpl;

public class PodFollowerFactory
{
    public PodFollower<PodPushClient> createFollower(KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig)
    {
        return new PodFollowerImpl<>(kubeConfig, podConfig, executionConfig);
    }
}
