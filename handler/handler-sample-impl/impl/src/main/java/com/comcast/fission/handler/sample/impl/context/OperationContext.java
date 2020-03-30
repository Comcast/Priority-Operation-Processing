package com.comcast.fission.handler.sample.impl.context;


import com.theplatform.dfh.cp.handler.base.context.ProgressOperationContext;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.reporter.ProgressReporter;

public class OperationContext extends ProgressOperationContext<LaunchDataWrapper>
{
    public OperationContext(ProgressReporter reporter, LaunchDataWrapper launchDataWrapper)
    {
        super(reporter, launchDataWrapper);
    }
}
