package com.comcast.pop.handler.reaper.impl.context;

import com.comast.pop.handler.base.context.BaseOperationContext;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The context for the Reaper
 */
public class ReaperContext extends BaseOperationContext<LaunchDataWrapper>
{
    private static Logger logger = LoggerFactory.getLogger(ReaperContext.class);

    public ReaperContext(LaunchDataWrapper launchDataWrapper)
    {
        super(launchDataWrapper);
    }

    @Override
    public void processUnhandledException(String s, Exception e)
    {
        logger.error("Failed to run reaper.", e);
    }
}
