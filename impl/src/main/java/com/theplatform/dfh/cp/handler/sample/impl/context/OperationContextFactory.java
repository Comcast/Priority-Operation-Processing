package com.theplatform.dfh.cp.handler.sample.impl.context;

import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.kubernetes.support.context.KubernetesOperationContextFactory;
import com.theplatform.dfh.cp.handler.reporter.log.LogReporter;

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
