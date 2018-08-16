package com.theplatform.dfh.cp.handler.executor.impl.executor.factory;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.base.perform.RetryableExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.context.HandlerContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes.KubernetesOperationExecutor;
import com.theplatform.dfh.filehandler.k8.PodFollower;
import com.theplatform.dfh.filehandler.k8.PodFollowerConfig;
import com.theplatform.dfh.filehandler.k8.PodPushClient;
import com.theplatform.dfh.filehandler.k8.config.KubeConfig;
import com.theplatform.dfh.filehandler.k8.factory.PodClientFactory;
import com.theplatform.dfh.filehandler.k8.modulation.CpuRequestModulation;
import com.theplatform.dfh.filehandler.k8.modulation.CpuRequestModulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Factory for producing executors to get MediaProperties (via a mediainfo launch through kubernetes)
 */
public class KubernetesOperationExecutorFactory implements OperationExecutorFactory
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesOperationExecutorFactory.class);

    private KubeConfig kubeConfig;
    private PodClientFactory<CpuRequestModulator> podClientFactory;
    private int podRetryDelay = 2000;

    public KubeConfig getKubeConfig()
    {
        return kubeConfig;
    }

    public void setKubeConfig(KubeConfig kubeConfig)
    {
        this.kubeConfig = kubeConfig;
    }

    public PodClientFactory<CpuRequestModulator> getPodClientFactory()
    {
        return podClientFactory;
    }

    public void setPodClientFactory(PodClientFactory<CpuRequestModulator> podClientFactory)
    {
        this.podClientFactory = podClientFactory;
    }

    @Override
    public BaseOperationExecutor getOperationExecutor(HandlerContext handlerContext, Operation operation)
    {
        String dockerImageName = handlerContext.getLaunchDataWrapper().getPropertyRetriever().getField("dockerImageName");
        PodFollower<PodPushClient> follower = podClientFactory.getPodFollower();
        PodFollowerConfig config = new PodFollowerConfig();
        follower.withFollowerConfig(config);
        PodPushClient wrapperClient = podClientFactory
            .getClient(kubeConfig, kubeConfig.getPodRetryCount(), TimeUnit.MILLISECONDS, podRetryDelay);
        wrapperClient.setCpuModulator(new CpuRequestModulator()
        {
            @Override
            public void setModulation(CpuRequestModulation cpuRequestModulation)
            {

            }

            @Override
            public String getCpuRequest()
            {
                return kubeConfig.getCpuMinimumRequestCount();
            }
        });

        KubernetesOperationExecutor operationExecutor = new KubernetesOperationExecutor(operation);
        operationExecutor.setFollower(follower);
        operationExecutor.setClient(wrapperClient);
        operationExecutor.setDockerImageName(dockerImageName);
        return operationExecutor;
    }
}