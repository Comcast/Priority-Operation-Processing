package com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes;

import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import org.slf4j.Logger;

public class TestLogKuberentesLauncher extends KubernetesLauncher
{
    public TestLogKuberentesLauncher(KubeConfig kubeConfig, PodConfig podConfig, ExecutionConfig executionConfig)
    {
        super(kubeConfig, podConfig, executionConfig);
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }
}
