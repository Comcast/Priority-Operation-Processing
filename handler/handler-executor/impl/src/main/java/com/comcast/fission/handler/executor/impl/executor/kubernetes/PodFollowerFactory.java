package com.comcast.fission.handler.executor.impl.executor.kubernetes;

import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.PodPushClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollower;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.follower.PodFollowerImpl;

public class PodFollowerFactory
{
    public PodFollower<PodPushClient> createFollower(KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig)
    {
        return new PodFollowerImpl<>(kubeConfig, podConfig, executionConfig);
    }
}
