package com.theplatform.dfh.cp.handler.executor.impl.progress.loader;

import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.base.field.api.args.HandlerArgument;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;

public class ProgressLoaderFactory
{
    public ProgressLoader createProgressLoader(ExecutorContext executorContext)
    {
        LaunchDataWrapper launchDataWrapper = executorContext.getLaunchDataWrapper();
        if(launchDataWrapper == null)
            return null;

        if(launchDataWrapper.getEnvironmentRetriever() != null
            && launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.LAST_PROGRESS.name()) != null)
        {
            return new EnvironmentProgressLoader(executorContext);
        }

        if(launchDataWrapper.getArgumentRetriever() != null
            && launchDataWrapper.getArgumentRetriever().getField(HandlerArgument.LAST_PROGRESS_FILE.getArgumentName()) != null)
        {
            return new FileProgressLoader(
                launchDataWrapper.getArgumentRetriever().getField(HandlerArgument.LAST_PROGRESS_FILE.getArgumentName()),
                executorContext);
        }
        return null;
    }
}
