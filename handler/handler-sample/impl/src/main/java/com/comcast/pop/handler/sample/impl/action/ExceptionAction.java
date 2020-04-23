package com.comcast.pop.handler.sample.impl.action;

import com.comast.pop.handler.base.progress.reporter.operation.OperationProgressReporter;
import com.comcast.pop.handler.sample.api.ActionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionAction extends BaseAction
{
    private static Logger logger = LoggerFactory.getLogger(ExceptionAction.class);

    @Override
    protected void perform(OperationProgressReporter reporter, ActionParameters actionParameters)
    {
        logger.info("About to throw a planned exception...");
        throw new RuntimeException(actionParameters.getExceptionMessage());
    }
}
