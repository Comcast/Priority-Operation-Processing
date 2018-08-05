package com.theplatform.dfh.cp.handler.sample.impl.action;

import com.theplatform.dfh.cp.handler.reporter.api.Reporter;
import com.theplatform.dfh.cp.handler.sample.api.ActionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionAction extends BaseAction
{
    private static Logger logger = LoggerFactory.getLogger(ExceptionAction.class);

    @Override
    protected void perform(Reporter reporter, ActionParameters actionParameters)
    {
        logger.info("About to throw a planned exception...");
        throw new RuntimeException(actionParameters.getExceptionMessage());
    }
}
