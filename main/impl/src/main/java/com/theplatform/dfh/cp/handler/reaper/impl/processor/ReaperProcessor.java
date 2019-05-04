package com.theplatform.dfh.cp.handler.reaper.impl.processor;

import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactoryImpl;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigField;
import com.theplatform.dfh.cp.handler.reaper.impl.context.ReaperContext;
import com.theplatform.dfh.cp.handler.reaper.impl.filter.ReapPodLookupFilter;
import com.theplatform.dfh.cp.handler.reaper.impl.kubernetes.KubernetesPodFacade;
import com.theplatform.dfh.cp.handler.reaper.impl.kubernetes.KubernetesPodFacadeImpl;
import com.theplatform.dfh.cp.handler.reaper.impl.property.ReaperProperty;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodPhase;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * Processor for reaping stale pods
 */
public class ReaperProcessor implements HandlerProcessor
{
    public static final String DEFAULT_NAMESPACE = "dfh";
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

        new BatchedPodReaper(
            new ReapPodLookupFilter(kubernetesPodFacade)
                .withNamespace(propertyRetriever.getField(KubeConfigField.NAMESPACE.getFieldName(), DEFAULT_NAMESPACE))
                .withReapPodAgeMinutes(propertyRetriever.getInt(ReaperProperty.POD_REAP_AGE_MINUTES, ReapPodLookupFilter.DEFAULT_POD_REAP_AGE_MINUTES))
                .withPodPhases(PodPhase.FAILED, PodPhase.SUCCEEDED),
            kubernetesPodFacade
        )
        .setPodReapBatchSize(propertyRetriever.getInt(ReaperProperty.POD_REAP_BATCH_SIZE, BatchedPodReaper.DEFAULT_POD_REAP_BATCH_SIZE))
        .execute();
    }
}
