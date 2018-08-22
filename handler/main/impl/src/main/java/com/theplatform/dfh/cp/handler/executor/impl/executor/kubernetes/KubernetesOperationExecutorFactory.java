package com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.executor.OperationExecutorFactory;

import com.theplatform.dfh.cp.handler.executor.impl.podconfig.registry.StaticPodConfigRegistryClient;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactory;
import com.theplatform.dfh.cp.modules.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.podconfig.registry.client.api.PodConfigRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for producing executors to get MediaProperties (via a mediainfo launch through kubernetes)
 */
public class KubernetesOperationExecutorFactory implements OperationExecutorFactory
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesOperationExecutorFactory.class);
    private PodConfigRegistryClient podConfigRegistryClient;
    private KubeConfigFactory kubeConfigFactory;

    public KubernetesOperationExecutorFactory()
    {
        this.podConfigRegistryClient = new StaticPodConfigRegistryClient();
    }

    @Override
    public BaseOperationExecutor createOperationExecutor(ExecutorContext executorContext, Operation operation)
    {
        KubeConfig kubeConfig = kubeConfigFactory.createKubeConfig();

        PodConfig podConfig = podConfigRegistryClient.getPodConfig(operation.getType());

        if(podConfig == null)
            throw new AgendaExecutorException(
                String.format("Unknown operation type found: %1$s on operation: %2$s", operation.getType(), operation.getName()));

        // cannot set the payload yet, it is processed and passed into the executor by whatever HandlerProcessor implementation
        // TODO: values should be settings from the properties file
        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix())
            .addEnvVar("LOG_LEVEL", "DEBUG");

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

    public KubernetesOperationExecutorFactory setPodConfigRegistryClient(PodConfigRegistryClient podConfigRegistryClient)
    {
        this.podConfigRegistryClient = podConfigRegistryClient;
        return this;
    }

    public KubeConfigFactory getKubeConfigFactory()
    {
        return kubeConfigFactory;
    }

    public KubernetesOperationExecutorFactory setKubeConfigFactory(KubeConfigFactory kubeConfigFactory)
    {
        this.kubeConfigFactory = kubeConfigFactory;
        return this;
    }
}