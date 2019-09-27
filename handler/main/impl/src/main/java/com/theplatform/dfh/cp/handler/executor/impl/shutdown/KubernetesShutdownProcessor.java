package com.theplatform.dfh.cp.handler.executor.impl.shutdown;

import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactory;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesShutdownProcessor implements ShutdownProcessor
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesShutdownProcessor.class);

    private KubeConfigFactory kubeConfigFactory;
    private FieldRetriever fieldRetriever;

    public KubernetesShutdownProcessor(KubeConfigFactory kubeConfigFactory, FieldRetriever fieldRetriever)
    {
        this.kubeConfigFactory = kubeConfigFactory;
        this.fieldRetriever = fieldRetriever;
    }

    @Override
    public void shutdown()
    {
        try
        {
            String executorPodName = fieldRetriever.getField(Fabric8Helper.MY_POD_NAME);
            if(executorPodName == null)
            {
                logger.debug("No executor pod name. Ignoring delete on shutdown.");
                return;
            }

            logger.debug("Creating kube config with no proxies for shutting down");

            KubeConfig kubeConfig = kubeConfigFactory.createKubeConfig();
            KubernetesClient kubernetesClient = new DefaultKubernetesClient(Fabric8Helper.getFabric8Config(kubeConfig));
            Pod pod = new PodBuilder()
                .withNewMetadata()
                    .withNamespace(kubeConfig.getNameSpace())
                    .withName(executorPodName)
                .endMetadata()
                .build();
            kubernetesClient.pods().delete(pod);
        }
        catch(Exception e)
        {
            logger.error("Failed to make pod delete request.", e);
            // silent, this is a shutdown
        }
    }
}
