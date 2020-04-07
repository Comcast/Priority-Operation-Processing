package com.comcast.pop.handler.puller.impl.limit;

import com.comcast.pop.handler.kubernetes.support.config.KubeConfigFactory;
import com.comcast.pop.handler.kubernetes.support.config.KubeConfigFactoryImpl;
import com.comcast.pop.handler.puller.impl.config.PullerConfig;
import com.comcast.pop.handler.puller.impl.context.PullerContext;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.LinkedList;
import java.util.List;

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
    public List<ResourceChecker> getResourceCheckers()
    {
        List<ResourceChecker> resourceCheckers = new LinkedList<>();
        assessInsighExecutionLimitChecker(resourceCheckers);
        return resourceCheckers;
    }

    protected void assessInsighExecutionLimitChecker(List<ResourceChecker> resourceCheckers)
    {
        Integer insightExecutionLimit = pullerContext.getLaunchDataWrapper().getPropertyRetriever()
            .getInt(KubernetesInsightExecutionResourceChecker.INSIGHT_EXECUTION_LIMIT_PROPERTY_NAME, -1);
        if(insightExecutionLimit != -1)
        {
            KubeConfig kubeConfig = kubeConfigFactory.createKubeConfig();
            KubernetesClient kubernetesClient = new DefaultKubernetesClient(Fabric8Helper.getFabric8Config(kubeConfig));
            PullerConfig pullerConfig = pullerContext.getLaunchDataWrapper().getPullerConfig();
            resourceCheckers.add(new KubernetesInsightExecutionResourceChecker(
                kubernetesClient,
                pullerConfig.getInsightId(),
                insightExecutionLimit));
        }
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
