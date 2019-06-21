package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.handler.field.retriever.DefaultLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.field.retriever.argument.ArgumentRetriever;
import com.theplatform.dfh.cp.handler.field.retriever.environment.EnvironmentFieldRetriever;
import com.theplatform.dfh.cp.handler.field.retriever.properties.PropertyRetriever;

public class KubernetesLaunchDataWrapper extends DefaultLaunchDataWrapper
{
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
}
