package com.theplatform.dfh.cp.handler.puller.impl.config;

import com.theplatform.dfh.cp.handler.field.retriever.DefaultLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.puller.impl.monitor.alive.LastRequestAliveCheck;

/**
 */
public class PullerLaunchDataWrapper extends DefaultLaunchDataWrapper
{
    private PullerConfig pullerConfig;

    private LastRequestAliveCheck lastRequestAliveCheck = new LastRequestAliveCheck();

    public PullerLaunchDataWrapper(FieldRetriever argumentRetriever)
    {
        super(argumentRetriever);
    }

    public String getPayload()
    {
        return null;  // Puller doesn't have a payload
    }

    public LastRequestAliveCheck getLastRequestAliveCheck()
    {
        return lastRequestAliveCheck;
    }

    public void setLastRequestAliveCheck(LastRequestAliveCheck lastRequestAliveCheck)
    {
        this.lastRequestAliveCheck = lastRequestAliveCheck;
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
