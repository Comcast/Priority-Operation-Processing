package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

/**
 * Property based PodConfig factory. Also supports a null property retriever, applying only the defaults when building a PodConfig.
 */
public class PodConfigFactoryImpl implements PodConfigFactory
{
    public static final String USE_TAINTED_NODES_PROPERTY = "cp.kubernetes.podconfig.useTaintedNodes";
    public static final String TAINTED_NODES_SELECTOR_PROPERTY = "cp.kubernetes.podconfig.taintedSelector";
    public static final String TAINTED_NODES_TOLERATION_PROPERTY = "cp.kubernetes.podconfig.taintedToleration";

    private final FieldRetriever fieldRetriever;

    public PodConfigFactoryImpl()
    {
        this.fieldRetriever = null;
    }

    public PodConfigFactoryImpl(FieldRetriever fieldRetriever)
    {
        this.fieldRetriever = fieldRetriever;
    }

    @Override
    public PodConfig createPodConfig()
    {
        return applyPropertyConfigValues(new PodConfig().applyDefaults());
    }

    /**
     * Returns the result of the createPodConfig() call
     * @param templateName Ignored
     * @return
     */
    @Override
    public PodConfig createPodConfig(String templateName)
    {
        return createPodConfig();
    }

    private PodConfig applyPropertyConfigValues(PodConfig podConfig)
    {
        if(fieldRetriever == null) return podConfig;

        podConfig.setUseTaintedNodes(fieldRetriever.getBoolean(USE_TAINTED_NODES_PROPERTY, false));
        if(podConfig.getUseTaintedNodes())
        {
            podConfig.setTaintedNodeSelector(fieldRetriever.getField(TAINTED_NODES_SELECTOR_PROPERTY));
            podConfig.setTaintedNodeToleration(fieldRetriever.getField(TAINTED_NODES_TOLERATION_PROPERTY));
        }
        return podConfig;
    }
}
