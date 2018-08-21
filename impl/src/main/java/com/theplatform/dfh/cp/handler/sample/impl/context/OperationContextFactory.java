package com.theplatform.dfh.cp.handler.sample.impl.context;

import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.kubernetes.support.context.KubernetesOperationContextFactory;

/**
 * Factory that creates a context object for this operation.
 */
public class OperationContextFactory extends KubernetesOperationContextFactory<OperationContext>
{
    public OperationContextFactory(LaunchDataWrapper launchDataWrapper)
    {
        super(launchDataWrapper);
    }

    @Override
    public OperationContext createOperationContext()
    {
        return new OperationContext(createKubernetesReporter(), launchDataWrapper);
    }
}
