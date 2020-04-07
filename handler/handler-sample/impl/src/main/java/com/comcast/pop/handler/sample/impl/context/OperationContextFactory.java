package com.comcast.pop.handler.sample.impl.context;

import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.reporter.LogReporter;
import com.comcast.pop.handler.kubernetes.support.context.KubernetesOperationContextFactory;

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
        switch(getLaunchType())
        {
            case local:
                return new OperationContext(new LogReporter(), launchDataWrapper);
            default:
                return new OperationContext(createKubernetesReporter(), launchDataWrapper);
        }
    }
}
