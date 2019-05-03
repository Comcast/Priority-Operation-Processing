package com.theplatform.dfh.cp.handler.puller.impl.config;

import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubernetesLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.puller.impl.monitor.alive.LastRequestAliveCheck;

/**
 */
public class PullerLaunchDataWrapper extends KubernetesLaunchDataWrapper
{
    private PullerConfig pullerConfig;

    private LastRequestAliveCheck lastRequestAliveCheck = new LastRequestAliveCheck();

    public PullerLaunchDataWrapper(FieldRetriever argumentRetriever)
    {
        super(argumentRetriever);
        lastRequestAliveCheck.setNotAliveThresholdMilliseconds(
            getPropertyRetriever().getLong(LastRequestAliveCheck.LAST_REQUEST_THRESHOLD_PROPERTY, 30000L));
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
