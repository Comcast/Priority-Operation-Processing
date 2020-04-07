package com.comcast.pop.handler.sample.impl.action;

import com.comcast.pop.handler.sample.impl.executor.BaseExternalExecutor;
import com.comcast.pop.handler.sample.impl.executor.ExternalExecutorFactory;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.progress.reporter.operation.OperationProgressReporter;
import com.comcast.pop.handler.sample.api.ActionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalExecuteAction extends BaseAction
{
    private static Logger logger = LoggerFactory.getLogger(ExternalExecuteAction.class);

    private LaunchDataWrapper launchDataWrapper;
    private ExternalExecutorFactory externalExecutorFactory;

    public ExternalExecuteAction(ExternalExecutorFactory externalExecutorFactory, LaunchDataWrapper launchDataWrapper)
    {
        this.externalExecutorFactory = externalExecutorFactory;
        this.launchDataWrapper = launchDataWrapper;
    }

    @Override
    protected void perform(OperationProgressReporter reporter, ActionParameters actionParameters)
    {
        try
        {
            BaseExternalExecutor externalExecutor = externalExecutorFactory.getExternalExecutor(launchDataWrapper, actionParameters.getExternalArgs());
            externalExecutor.execute();
        }
        catch(Exception e)
        {
            logger.error("Failed to run external execution. Are you setup for the correct environment? (Kubernetes or otherwise)", e);
        }
    }
}
