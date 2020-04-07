package com.comcast.pop.handler.reaper.impl.context;

import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.reporter.LogReporter;
import com.comast.pop.handler.base.reporter.ProgressReporter;
import com.comcast.pop.handler.kubernetes.support.context.KubernetesOperationContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that creates a context object for this operation.
 */
public class ReaperContextFactory extends KubernetesOperationContextFactory<ReaperContext>
{
    private static Logger logger = LoggerFactory.getLogger(ReaperContextFactory.class);

    public ReaperContextFactory(LaunchDataWrapper launchDataWrapper)
    {
        super(launchDataWrapper);
    }

    @Override
    public ReaperContext createOperationContext()
    {
        return new ReaperContext(launchDataWrapper);
    }

    @Override
    public ProgressReporter createReporter()
    {
        return new LogReporter();
    }
}
