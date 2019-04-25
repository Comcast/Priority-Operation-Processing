package com.theplatform.dfh.cp.handler.reaper.impl.processor;

import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactoryImpl;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigField;
import com.theplatform.dfh.cp.handler.reaper.impl.context.ReaperContext;
import com.theplatform.dfh.cp.handler.reaper.impl.filter.ReapPodLookupFilter;
import com.theplatform.dfh.cp.handler.reaper.impl.filter.PodLookupFilter;
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

import java.util.List;

/**
 * AgendaProcessor that will execute the operations in parallel where possible.
 */
public class ReaperProcessor implements HandlerProcessor
{
    public static final String DEFAULT_NAMESPACE = "dfh";

    private static Logger logger = LoggerFactory.getLogger(ReaperProcessor.class);

    private final ReaperContext reaperContext;
    private PodLookupFilter podLookupFilter;
    private KubernetesPodFacade kubernetesPodFacade;

    public ReaperProcessor(ReaperContext reaperContext)
    {
        this.reaperContext = reaperContext;
        KubeConfig kubeConfig = new KubeConfigFactoryImpl(reaperContext.getLaunchDataWrapper()).createKubeConfig();
        KubernetesClient kubernetesClient = new DefaultKubernetesClient(Fabric8Helper.getFabric8Config(kubeConfig));
        kubernetesPodFacade = new KubernetesPodFacadeImpl(kubernetesClient);

        FieldRetriever propertyRetriever = reaperContext.getLaunchDataWrapper().getPropertyRetriever();

        this.podLookupFilter = new ReapPodLookupFilter(kubernetesPodFacade)
            .withNamespace(propertyRetriever.getField(KubeConfigField.NAMESPACE.getFieldName(), DEFAULT_NAMESPACE))
            .withReapPodAgeMinutes(propertyRetriever.getInt(ReaperProperty.POD_REAP_AGE_MINUTES, ReapPodLookupFilter.DEFAULT_POD_REAP_AGE_MINUTES))
            .withPodPhases(PodPhase.FAILED, PodPhase.SUCCEEDED);
    }

    @Override
    public void execute()
    {
        List<Pod> podsToDelete = podLookupFilter.performLookup();
        if(podsToDelete == null || podsToDelete.size() == 0)
        {
            logger.info("No pods found for reaping.");
        }
        else
        {
            // TODO: break this up into smaller sets?
            podsToDelete.forEach(x -> logger.info("Attempting to delete pod: {}", x.getMetadata().getName()));
            kubernetesPodFacade.deletePods(podsToDelete);
        }
        try
        {
            Thread.sleep(10000);
        }
        catch(InterruptedException e)
        {
            // this is just a test for syslog
        }
    }
}
