package com.theplatform.dfh.cp.handler.executor.impl.context;

import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes.KubernetesOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.local.LocalOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.OperationExecutorFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.kubernetes.support.context.KubernetesOperationContextFactory;

/**
 * Factory that creates a context object for this operation. This allows the command line to override the type of executor to use.
 */
public class ExecutorContextFactory extends KubernetesOperationContextFactory<ExecutorContext>
{
    public ExecutorContextFactory(LaunchDataWrapper launchDataWrapper)
    {
        super(launchDataWrapper);
    }

    @Override
    public ExecutorContext getOperationContext()
    {
        OperationExecutorFactory operationExecutorFactory;

        switch (getExternalLaunchType())
        {
            case local:
                operationExecutorFactory = new LocalOperationExecutorFactory();
                break;
            case docker:
                // TODO: decide if we want to support docker ops execution...
                throw new AgendaExecutorException("Docker is not supported for agenda execution.");
            case kubernetes:
            default:
                operationExecutorFactory = new KubernetesOperationExecutorFactory()
                    .setKubeConfigFactory(getKubeConfigFactory());
                break;
        }

        return new ExecutorContext(createReporter(), launchDataWrapper, operationExecutorFactory);
    }
}
