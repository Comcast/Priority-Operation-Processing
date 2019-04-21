package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.handler.field.retriever.DefaultLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.field.retriever.argument.ArgumentRetriever;

public class KubernetesLaunchDataWrapper extends DefaultLaunchDataWrapper
{
    public KubernetesLaunchDataWrapper(String[] args)
    {
        super(new ArgumentRetriever(new KubernetesArgumentProvider(args)));
    }

    public KubernetesLaunchDataWrapper(FieldRetriever argumentRetriever,
        FieldRetriever environmentRetriever, FieldRetriever propertyRetriever)
    {
        super(argumentRetriever, environmentRetriever, propertyRetriever);
    }

    public KubernetesLaunchDataWrapper(FieldRetriever argumentRetriever)
    {
        super(argumentRetriever);
    }
}
