package com.comcast.pop.handler.sample.impl.context;


import com.comast.pop.handler.base.context.ProgressOperationContext;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.reporter.ProgressReporter;

public class OperationContext extends ProgressOperationContext<LaunchDataWrapper>
{
    public OperationContext(ProgressReporter reporter, LaunchDataWrapper launchDataWrapper)
    {
        super(reporter, launchDataWrapper);
    }
}
