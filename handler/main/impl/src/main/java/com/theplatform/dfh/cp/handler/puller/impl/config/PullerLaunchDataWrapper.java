package com.theplatform.dfh.cp.handler.puller.impl.config;

import com.theplatform.dfh.cp.handler.field.retriever.DefaultLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;

/**
 */
public class PullerLaunchDataWrapper extends DefaultLaunchDataWrapper
{
    private PullerConfig pullerConfig;

    public PullerLaunchDataWrapper(FieldRetriever argumentRetriever)
    {
        super(argumentRetriever);
    }

    public String getPayload()
    {
        return null;  // Puller doesn't have a payload
    }

    public PullerConfig getPullerConfig()
    {
        return pullerConfig;
    }

    public PullerLaunchDataWrapper setPullerConfig(PullerConfig pullerConfig)
    {
        this.pullerConfig = pullerConfig;
        return this;
    }
}
