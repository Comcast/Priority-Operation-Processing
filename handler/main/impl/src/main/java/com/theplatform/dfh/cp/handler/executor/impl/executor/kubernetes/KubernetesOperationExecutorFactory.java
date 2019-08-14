package com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.executor.OperationExecutorFactory;

import com.theplatform.dfh.cp.handler.executor.impl.registry.podconfig.StaticPodConfigRegistryClient;
import com.theplatform.dfh.cp.handler.executor.impl.registry.podconfig.StaticProdPodConfigRegistryClient;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactoryImpl;
import com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry.JsonPodConfigRegistryClient;
import com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry.PodConfigRegistryClient;
import com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry.api.PodConfigRegistryClientException;
import com.theplatform.dfh.cp.modules.kube.client.CpuRequestModulator;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
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

    public KubernetesOperationExecutorFactory(LaunchDataWrapper launchDataWrapper)
    {
        boolean useStaticRegistryClient = launchDataWrapper.getPropertyRetriever().getBoolean("useStaticRegistryClient", false);

        if (useStaticRegistryClient)
        {
            this.podConfigRegistryClient = new StaticPodConfigRegistryClient();
        }
        else
        {
            this.podConfigRegistryClient = new JsonPodConfigRegistryClient("/app/config/registry.json");
        }
    }

    @Override
    public BaseOperationExecutor createOperationExecutor(ExecutorContext executorContext, Operation operation)
    {
        // This is a call to the local K8s API. We can't use
        // any proxies. Make sure that we are not.
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");

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

        return buildOperationExecutor(podConfig, operation, kubeConfig, executorContext.getLaunchDataWrapper());
    }

    private BaseOperationExecutor buildOperationExecutor(PodConfig podConfig, Operation operation, KubeConfig kubeConfig, LaunchDataWrapper launchDataWrapper) {
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

        // Make sure we don't use proxies
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");

        return new KubernetesOperationExecutor(operation, kubeConfig, podConfig, executionConfig, launchDataWrapper);
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