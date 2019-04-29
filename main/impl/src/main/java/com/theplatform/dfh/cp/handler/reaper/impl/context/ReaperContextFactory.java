package com.theplatform.dfh.cp.handler.reaper.impl.context;

import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.kubernetes.support.context.KubernetesOperationContextFactory;
import com.theplatform.dfh.cp.handler.reporter.api.ProgressReporter;
import com.theplatform.dfh.cp.handler.reporter.log.LogReporter;
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
