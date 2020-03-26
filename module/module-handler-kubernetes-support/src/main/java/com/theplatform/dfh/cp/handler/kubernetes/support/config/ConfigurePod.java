package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.handler.base.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

import java.util.Optional;

public interface ConfigurePod
{
    void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever);
}
