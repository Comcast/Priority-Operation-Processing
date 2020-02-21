package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.handler.base.field.api.args.HandlerArgument;
import com.theplatform.dfh.cp.handler.base.field.retriever.DefaultLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.field.retriever.argument.ArgumentRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.environment.EnvironmentFieldRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.properties.PropertyRetriever;
import com.theplatform.dfh.cp.handler.base.payload.PayloadReader;
import com.theplatform.dfh.cp.handler.base.payload.PayloadReaderFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.payload.PayloadReaderFactoryImpl;

public class KubernetesLaunchDataWrapper extends DefaultLaunchDataWrapper
{
    private PayloadReaderFactory payloadReaderFactory = new PayloadReaderFactoryImpl(this);

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
        // load from file if available
        String payloadFromFile = getStringFromFileArg(HandlerArgument.PAYLOAD_FILE);
        if(payloadFromFile != null) return payloadFromFile;

        // load from the associated reader
        PayloadReader payloadReader = payloadReaderFactory.createReader();
        return payloadReader.readPayload();
    }

    public void setPayloadReaderFactory(PayloadReaderFactory payloadReaderFactory)
    {
        this.payloadReaderFactory = payloadReaderFactory;
    }
}
