package com.comcast.pop.handler.puller.impl.executor.kubernetes;

import com.comcast.pop.modules.kube.client.CpuRequestModulator;
import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import com.comcast.pop.modules.kube.fabric8.client.PodPushClient;
import com.comcast.pop.modules.kube.fabric8.client.factory.PodFollowerFactory;
import com.comcast.pop.modules.kube.fabric8.client.factory.PodPushClientFactory;
import com.comcast.pop.modules.kube.fabric8.client.follower.PodFollower;
import org.slf4j.Logger;

public class TestLogKuberentesLauncher extends KubernetesLauncher
{
    public TestLogKuberentesLauncher(KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig)
    {
        super(kubeConfig, podConfig, executionConfig, null, new TestPodPushClientFactory(), new TestPodFollowerFactory());
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    static class TestPodFollowerFactory extends PodFollowerFactory<PodPushClient>
    {
        @Override
        public PodFollower<PodPushClient> createPodFollower(KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig)
        {
            return null;
        }
    }

    static class TestPodPushClientFactory extends PodPushClientFactory<CpuRequestModulator>
    {
        @Override
        public PodPushClient getClient(KubeConfig kubeConfig)
        {
            return null;
        }
    }
}
