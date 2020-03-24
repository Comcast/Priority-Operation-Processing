package com.theplatform.dfh.cp.handler.reaper.impl.processor;

import com.theplatform.com.dfh.modules.sync.util.Consumer;
import com.theplatform.com.dfh.modules.sync.util.Producer;
import com.theplatform.com.dfh.modules.sync.util.SynchronousProducerConsumerProcessor;
import com.theplatform.dfh.cp.handler.base.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactoryImpl;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigField;
import com.theplatform.dfh.cp.handler.reaper.impl.context.ReaperContext;
import com.theplatform.dfh.cp.handler.reaper.impl.delete.BatchedPodDeleter;
import com.theplatform.dfh.cp.handler.reaper.impl.filter.ReapPodLookupFilter;
import com.theplatform.dfh.cp.handler.reaper.impl.kubernetes.KubernetesPodFacade;
import com.theplatform.dfh.cp.handler.reaper.impl.kubernetes.KubernetesPodFacadeImpl;
import com.theplatform.dfh.cp.handler.reaper.impl.property.ReaperProperty;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodPhase;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Processor for reaping stale pods
 */
public class ReaperProcessor implements HandlerProcessor
{
    private static Logger logger = LoggerFactory.getLogger(ReapPodLookupFilter.class);

    public static final String DEFAULT_NAMESPACE = "dfh";
    private static final int DEFAULT_REAPER_RUN_MAX_MINUTES = 5;
    private ReaperContext reaperContext;

    public ReaperProcessor(ReaperContext reaperContext)
    {
        this.reaperContext = reaperContext;
    }

    @Override
    public void execute()
    {
        FieldRetriever propertyRetriever = reaperContext.getLaunchDataWrapper().getPropertyRetriever();

        KubeConfig kubeConfig = new KubeConfigFactoryImpl(reaperContext.getLaunchDataWrapper()).createKubeConfig();
        KubernetesClient kubernetesClient = new DefaultKubernetesClient(Fabric8Helper.getFabric8Config(kubeConfig));
        KubernetesPodFacade kubernetesPodFacade = new KubernetesPodFacadeImpl(kubernetesClient);

        Instant reapUpperBoundUTC = Instant.now().minusSeconds(
            60 * propertyRetriever.getInt(ReaperProperty.POD_REAP_AGE_MINUTES, ReapPodLookupFilter.DEFAULT_POD_REAP_AGE_MINUTES)
        );

        final int MAX_RUNTIME_SECONDS = 60 * propertyRetriever.getInt(ReaperProperty.REAPER_RUN_MAX_MINUTES, DEFAULT_REAPER_RUN_MAX_MINUTES);

        Producer<Pod> producer = new ReapPodLookupFilter(kubernetesPodFacade, reapUpperBoundUTC)
            .withNamespace(propertyRetriever.getField(KubeConfigField.NAMESPACE.getFieldName(), DEFAULT_NAMESPACE))
            .withPodPhases(PodPhase.FAILED, PodPhase.SUCCEEDED);

        Consumer<Pod> consumer = new BatchedPodDeleter(kubernetesPodFacade)
            .setPodReapBatchSize(propertyRetriever.getInt(ReaperProperty.POD_REAP_BATCH_SIZE, BatchedPodDeleter.DEFAULT_POD_REAP_BATCH_SIZE));

        SynchronousProducerConsumerProcessor<Pod> processor = new SynchronousProducerConsumerProcessor<>(
            producer,
            consumer
        )
        .setRunMaxSeconds(MAX_RUNTIME_SECONDS);

        logger.info("Running reaper for up to {} seconds. {} {}", MAX_RUNTIME_SECONDS, producer.toString(), consumer.toString());

        processor.execute();
    }
}
