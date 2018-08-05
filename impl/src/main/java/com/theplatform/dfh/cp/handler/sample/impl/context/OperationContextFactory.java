package com.theplatform.dfh.cp.handler.sample.impl.context;

import com.theplatform.dfh.cp.handler.field.api.LaunchType;
import com.theplatform.dfh.cp.handler.field.api.args.HandlerArgument;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.kubernetes.KubernetesReporterSet;
import com.theplatform.dfh.cp.handler.reporter.log.LogReporter;

/**
 * Factory that creates a context object for this operation. This allows the command line to override the type of executor to use.
 */
public class OperationContextFactory
{
    public OperationContext getOperationConfiguration(LaunchDataWrapper launchDataWrapper)
    {
        LaunchType launchType;

        try
        {
            launchType = Enum.valueOf(LaunchType.class,
                launchDataWrapper.getArgumentRetriever().getField(HandlerArgument.LAUNCH_TYPE.getArgumentName(), LaunchType.kubernetes.name()));
        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to evaluate launchType");
        }
        switch (launchType)
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
