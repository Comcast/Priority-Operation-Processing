package com.comcast.pop.handler.kubernetes.support.config;

import com.comast.pop.handler.base.field.api.args.HandlerArgument;
import com.comast.pop.handler.base.field.retriever.DefaultLaunchDataWrapper;
import com.comast.pop.handler.base.field.retriever.argument.ArgumentRetriever;
import com.comast.pop.handler.base.field.retriever.environment.EnvironmentFieldRetriever;
import com.comast.pop.handler.base.field.retriever.properties.PropertyRetriever;
import com.comast.pop.handler.base.payload.PayloadReader;
import com.comast.pop.handler.base.payload.PayloadReaderFactory;
import com.comcast.pop.handler.kubernetes.support.payload.PayloadReaderFactoryImpl;

/**
 * Specialized Kubernetes version of the LaunchDataWrapper (provides a couple of extra arguments and special payload loading)
 */
public class KubernetesLaunchDataWrapper extends DefaultLaunchDataWrapper
{
    private PayloadReaderFactory payloadReaderFactory = new PayloadReaderFactoryImpl(this);
    private String payload = null;

    public KubernetesLaunchDataWrapper(String[] args)
    {
        super(new ArgumentRetriever(new KubernetesArgumentProvider(args)));
    }

    public KubernetesLaunchDataWrapper(ArgumentRetriever argumentRetriever,
        EnvironmentFieldRetriever environmentRetriever, PropertyRetriever propertyRetriever)
    {
        super(argumentRetriever, environmentRetriever, propertyRetriever);
    }

    public KubernetesLaunchDataWrapper(ArgumentRetriever argumentRetriever)
    {
        super(argumentRetriever);
    }

    @Override
    public String getPayload()
    {
        // return the cached copy if there is one
        if(payload != null) return payload;

        // load from file if available
        payload = getStringFromFileArg(HandlerArgument.PAYLOAD_FILE);
        if(payload != null) return payload;

        // load from the associated reader
        PayloadReader payloadReader = payloadReaderFactory.createReader();
        payload = payloadReader.readPayload();
        return payload;
    }

    public void setPayloadReaderFactory(PayloadReaderFactory payloadReaderFactory)
    {
        this.payloadReaderFactory = payloadReaderFactory;
    }

    public void resetCachedPayload()
    {
        payload = null;
    }
}
