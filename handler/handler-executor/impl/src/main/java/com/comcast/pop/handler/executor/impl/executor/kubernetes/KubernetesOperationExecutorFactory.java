package com.comcast.pop.handler.executor.impl.executor.kubernetes;

import com.comcast.pop.handler.executor.impl.exception.AgendaExecutorException;
import com.comcast.pop.handler.executor.impl.processor.OperationWrapper;
import com.comcast.pop.api.operation.Operation;
import com.comast.pop.handler.base.field.api.HandlerField;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.comcast.pop.handler.executor.impl.executor.BaseOperationExecutor;
import com.comcast.pop.handler.executor.impl.executor.OperationExecutorFactory;

import com.comcast.pop.handler.executor.impl.registry.podconfig.StaticPodConfigRegistryClient;
import com.comcast.pop.handler.executor.impl.registry.podconfig.StaticProdPodConfigRegistryClient;
import com.comcast.pop.handler.kubernetes.support.config.KubeConfigFactory;
import com.comcast.pop.handler.kubernetes.support.podconfig.client.registry.JsonPodConfigRegistryClient;
import com.comcast.pop.handler.kubernetes.support.podconfig.client.registry.PodConfigRegistryClient;
import com.comcast.pop.handler.kubernetes.support.podconfig.client.registry.api.PodConfigRegistryClientException;
import com.comcast.pop.modules.kube.client.CpuRequestModulator;
import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for producing kubernetes based operation executors
 */
public class KubernetesOperationExecutorFactory extends OperationExecutorFactory
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesOperationExecutorFactory.class);
    private PodConfigRegistryClient podConfigRegistryClient;
    private KubeConfigFactory kubeConfigFactory;
    private PodFollowerFactory podFollowerFactory = new PodFollowerFactory();

    public KubernetesOperationExecutorFactory(LaunchDataWrapper launchDataWrapper)
    {
        boolean useStaticRegistryClient = launchDataWrapper.getPropertyRetriever().getBoolean("useStaticRegistryClient", false);

        if (useStaticRegistryClient)
        {
            if(launchDataWrapper.getPropertyRetriever().getBoolean("useStaticProdRegistryClient", false))
                this.podConfigRegistryClient = new StaticProdPodConfigRegistryClient();
            else
                this.podConfigRegistryClient = new StaticPodConfigRegistryClient();
        }
        else
        {
            this.podConfigRegistryClient = new JsonPodConfigRegistryClient("/app/config/registry.json");
        }
    }

    @Override
    public BaseOperationExecutor createOperationExecutor(ExecutorContext executorContext, OperationWrapper operationWrapper)
    {
        Operation operation = operationWrapper.getOperation();

        logger.info("Creating kube config with no proxies");
        KubeConfig kubeConfig = kubeConfigFactory.createKubeConfig();

        PodConfig podConfig = null;
        try {
            podConfig = podConfigRegistryClient.getPodConfig(operation.getType());
        } catch (PodConfigRegistryClientException e) {
            logger.error("There was a problem trying to retrieve the PodConfig {} from PodConfigRegistryClient.",
                operation == null ? "unknown" : operation.getType());
        }

        if(podConfig == null) {
            logger.error("Could not retrieve PodConfig from Registry..");
            throw new AgendaExecutorException(
                    String.format("Unknown operation type found: %1$s on operation: %2$s", operation.getType(), operation.getName()));
        }

        if(operationWrapper.getPriorExecutionOperationProgress() != null)
        {
            logger.info("Found prior operationProgress for op: {}", operationWrapper.getOperation().getName());
            podConfig.addEnvVars(HandlerField.LAST_PROGRESS.name(),
                executorContext.getJsonHelper().getJSONString(operationWrapper.getPriorExecutionOperationProgress()));
        }

        return buildOperationExecutor(podConfig, operation, kubeConfig, executorContext);
    }

    private BaseOperationExecutor buildOperationExecutor(PodConfig podConfig, Operation operation, KubeConfig kubeConfig, ExecutorContext executorContext) {
        // cannot set the payload yet, it is processed and passed into the executor by whatever HandlerProcessor implementation
        // TODO: values should be settings from the properties file
        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix())
                .addEnvVar("LOG_LEVEL", "INFO");

        executionConfig.setCpuRequestModulator(new CpuRequestModulator()
        {
            @Override
            public String getCpuRequest()
            {
                return podConfig.getCpuMinRequestCount();
            }

            @Override
            public String getCpuLimit()
            {
                return podConfig.getCpuMaxRequestCount();
            }
        });

        return new KubernetesOperationExecutor(
            podFollowerFactory.createFollower(kubeConfig, podConfig, executionConfig), operation, kubeConfig, podConfig, executionConfig, executorContext);
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

    public PodFollowerFactory getPodFollowerFactory()
    {
        return podFollowerFactory;
    }

    public void setPodFollowerFactory(PodFollowerFactory podFollowerFactory)
    {
        this.podFollowerFactory = podFollowerFactory;
    }
}