package com.comcast.pop.handler.kubernetes.support.config;

import com.comast.pop.handler.base.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

import java.util.Arrays;
import java.util.Optional;

/**
 * Property-based PodConfig factory. Also supports a null property retriever, applying only the defaults when building a PodConfig.
 */
public class PodConfigFactoryImpl implements PodConfigFactory
{
    private final Optional<FieldRetriever> fieldRetriever;

    public PodConfigFactoryImpl()
    {
        this.fieldRetriever = Optional.empty();
    }

    public PodConfigFactoryImpl(FieldRetriever fieldRetriever)
    {
        this.fieldRetriever = Optional.ofNullable(fieldRetriever);
    }

    @Override
    public PodConfig createPodConfig()
    {
        return applyPropertyConfigValues(new PodConfig().applyDefaults());
    }

    @Override
    public PodConfig createPodConfig(String templateName)
    {
        return createPodConfig();
    }

    private PodConfig applyPropertyConfigValues(PodConfig podConfig)
    {
        Arrays.stream(PodConfigStations.values()).forEach(field -> field.setPodConfig(podConfig, fieldRetriever));
        return podConfig;
    }
}
