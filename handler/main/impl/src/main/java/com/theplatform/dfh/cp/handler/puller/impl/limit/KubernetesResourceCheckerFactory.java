package com.theplatform.dfh.cp.handler.puller.impl.limit;

import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubeConfigFactoryImpl;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerConfig;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContext;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesResourceCheckerFactory implements ResourceCheckerFactory
{
    private PullerContext pullerContext;
    private KubeConfigFactory kubeConfigFactory;

    public KubernetesResourceCheckerFactory()
    {

    }

    public KubernetesResourceCheckerFactory(PullerContext pullerContext)
    {
        this.pullerContext = pullerContext;
        this.kubeConfigFactory = new KubeConfigFactoryImpl(pullerContext.getLaunchDataWrapper());
    }

    @Override
    public ResourceChecker getResourceChecker()
    {
        PullerLaunchDataWrapper pullerLaunchDataWrapper = pullerContext.getLaunchDataWrapper();
        Integer insightExecutionLimit = pullerLaunchDataWrapper.getPropertyRetriever().getInt(KubernetesInsightExecutionResourceChecker.LIMIT_PROPERTY_NAME, -1);
        if(insightExecutionLimit != -1)
        {
            KubeConfig kubeConfig = kubeConfigFactory.createKubeConfig();
            KubernetesClient kubernetesClient = new DefaultKubernetesClient(Fabric8Helper.getFabric8Config(kubeConfig));
            PullerConfig pullerConfig = pullerContext.getLaunchDataWrapper().getPullerConfig();
            return new KubernetesInsightExecutionResourceChecker(
                kubernetesClient,
                pullerConfig.getInsightId(),
                insightExecutionLimit);
        }
        return null;
    }

    public KubernetesResourceCheckerFactory setPullerContext(PullerContext pullerContext)
    {
        this.pullerContext = pullerContext;
        return this;
    }

    public KubernetesResourceCheckerFactory setKubeConfigFactory(KubeConfigFactory kubeConfigFactory)
    {
        this.kubeConfigFactory = kubeConfigFactory;
        return this;
    }
}
