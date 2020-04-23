package com.comcast.pop.handler.kubernetes.support.config;

import com.comast.pop.handler.base.field.retriever.api.FieldRetriever;
import com.comcast.pop.modules.kube.client.config.PodConfig;

import java.util.Optional;

public interface ConfigurePod
{
    void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever);
}
