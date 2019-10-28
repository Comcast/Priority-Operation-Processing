package com.theplatform.dfh.cp.handler.sample.impl.context;

import com.theplatform.dfh.cp.handler.base.context.ProgressOperationContext;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.ProgressReporter;

public class OperationContext extends ProgressOperationContext<LaunchDataWrapper>
{
    public OperationContext(ProgressReporter reporter, LaunchDataWrapper launchDataWrapper)
    {
        super(reporter, launchDataWrapper);
    }
}
