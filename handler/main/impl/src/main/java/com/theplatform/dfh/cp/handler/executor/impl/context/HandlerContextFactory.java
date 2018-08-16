package com.theplatform.dfh.cp.handler.executor.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.executor.factory.KubernetesOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.factory.LocalOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporterSet;
import com.theplatform.dfh.cp.handler.reporter.log.LogReporter;

/**
 * Factory that creates a context object for this operation. This allows the command line to override the type of executor to use.
 */
public class HandlerContextFactory extends BaseOperationContextFactory<HandlerContext>
{
    public HandlerContextFactory(LaunchDataWrapper launchDataWrapper)
    {
        super(launchDataWrapper);
    }

    @Override
    public HandlerContext getOperationContext()
    {
        switch (getExternalLaunchType())
        {
            case local:
                return new HandlerContext(new LogReporter(), launchDataWrapper, new LocalOperationExecutorFactory());
            case docker:
                // TODO: decide if we want to support docker ops execution...
                throw new AgendaExecutorException("Docker is not supported for agenda execution.");
            case kubernetes:
            default:
                return new HandlerContext(new KubernetesReporterSet(), launchDataWrapper, new KubernetesOperationExecutorFactory());
        }
    }
}
