package com.theplatform.dfh.cp.handler.executor.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporterSet;
import com.theplatform.dfh.cp.handler.reporter.log.LogReporter;

/**
 * Factory that creates a context object for this operation. This allows the command line to override the type of executor to use.
 */
public class OperationContextFactory extends BaseOperationContextFactory<OperationContext>
{
    public OperationContextFactory(LaunchDataWrapper launchDataWrapper)
    {
        super(launchDataWrapper);
    }

    @Override
    public OperationContext getOperationContext()
    {
        switch (getExternalLaunchType())
        {
            case local:
                return new OperationContext(new LogReporter());
            case docker:
                return new OperationContext(new LogReporter());
            case kubernetes:
            default:
                return new OperationContext(new KubernetesReporterSet());
        }
    }
}
