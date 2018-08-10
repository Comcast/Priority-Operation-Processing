package com.theplatform.dfh.cp.handler.executor.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.factory.DockerAgendaExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.factory.KubernetesAgendaExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.factory.LocalAgendaExecutorFactory;
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
                return new OperationContext(new LocalAgendaExecutorFactory(), new LogReporter());
            case docker:
                return new OperationContext(new DockerAgendaExecutorFactory(), new LogReporter());
            case kubernetes:
            default:
                return new OperationContext(new KubernetesAgendaExecutorFactory(), new KubernetesReporterSet());
        }
    }
}
