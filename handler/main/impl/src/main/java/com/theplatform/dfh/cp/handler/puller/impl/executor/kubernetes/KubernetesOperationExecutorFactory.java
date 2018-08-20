package com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.puller.impl.context.HandlerContext;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.puller.impl.executor.OperationExecutorFactory;

import com.theplatform.dfh.cp.modules.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for producing executors to get MediaProperties (via a mediainfo launch through kubernetes)
 */
public class KubernetesOperationExecutorFactory implements OperationExecutorFactory
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesOperationExecutorFactory.class);

    private int podRetryDelay = 2000;

    @Override
    public BaseOperationExecutor getOperationExecutor(HandlerContext handlerContext, Operation operation)
    {
        // TODO: decide how much needs to be setup here vs. in the kube puller itself

        // TODO: this should come from the registry lookup
        final String dockerImageName = "docker-lab.repo.theplatform.com/fhsamp:1.0.0";
        // TODO: need a launchdatawrapper -> kubeconfig helper
        KubeConfig kubeConfig = new KubeConfig()
            .setMasterUrl(handlerContext.getLaunchDataWrapper().getEnvironmentRetriever().getField("K8_MASTER_URL"))
            .setNameSpace("dfh");

        PodConfig podConfig = new PodConfig()
            .setServiceAccountName("ffmpeg-service")
            .setMemoryRequestCount("1000m")
            .setCpuMinRequestCount("1000m")
            .setCpuMaxRequestCount("1000m")
            .setPodScheduledTimeoutMs(600000L)
            .setReapCompletedPods(true)
            .setPullAlways(true) // for now
            .setImageName(dockerImageName)
            .setNamePrefix("dfh-samp");

        // TODO: cannot set the payload yet, it is processed and passed into the puller by whatever HandlerProcessor implementation
        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix())
            .addEnvVar("LOG_LEVEL", "DEBUG")
            .addEnvVar("K8_MASTER_URL", kubeConfig.getMasterUrl());

        executionConfig.setCpuRequestModulator(new CpuRequestModulator()
        {
            @Override
            public String getCpuRequest()
            {
                return podConfig.getCpuMinRequestCount();
            }
        });


        return new KubernetesOperationExecutor(operation, kubeConfig, podConfig, executionConfig);
    }
}