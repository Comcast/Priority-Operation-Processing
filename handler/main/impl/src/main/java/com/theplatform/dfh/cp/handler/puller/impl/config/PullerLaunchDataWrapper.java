package com.theplatform.dfh.cp.handler.puller.impl.config;

import com.theplatform.dfh.cp.handler.base.field.retriever.argument.ArgumentRetriever;
import com.theplatform.dfh.cp.handler.base.payload.PayloadWriterFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubernetesLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.kubernetes.support.payload.PayloadWriterFactoryImpl;
import com.theplatform.dfh.cp.handler.puller.impl.monitor.alive.LastRequestAliveCheck;

/**
 */
public class PullerLaunchDataWrapper extends KubernetesLaunchDataWrapper
{
    private PullerConfig pullerConfig;
    private LastRequestAliveCheck lastRequestAliveCheck = new LastRequestAliveCheck();

    private PayloadWriterFactory payloadWriterFactory = new PayloadWriterFactoryImpl(this);

    public PullerLaunchDataWrapper(ArgumentRetriever argumentRetriever)
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

    public PayloadWriterFactory getPayloadWriterFactory()
    {
        return payloadWriterFactory;
    }

    public void setPayloadWriterFactory(PayloadWriterFactory payloadWriterFactory)
    {
        this.payloadWriterFactory = payloadWriterFactory;
    }
}
