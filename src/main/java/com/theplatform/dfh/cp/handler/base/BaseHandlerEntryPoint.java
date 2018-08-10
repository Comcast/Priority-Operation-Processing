package com.theplatform.dfh.cp.handler.base;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

public abstract class BaseHandlerEntryPoint<C extends BaseOperationContext, P extends HandlerProcessor>
{
    private LaunchDataWrapper launchDataWrapper;
    private BaseOperationContextFactory<C> operationContextFactory;

    protected abstract LaunchDataWrapper createLaunchDataWrapper(String[] args);
    protected abstract BaseOperationContextFactory<C> createOperationContextFactory(LaunchDataWrapper launchDataWrapper);
    protected abstract P createHandlerProcessor(LaunchDataWrapper launchDataWrapper, C operationContext);

    public BaseHandlerEntryPoint(String[] args)
    {
        // gather the inputs args, environment, properties
        launchDataWrapper = createLaunchDataWrapper(args);
        operationContextFactory = createOperationContextFactory(launchDataWrapper);
    }

    public void execute()
    {
        // get the operation specific context for running the overall process
        createHandlerProcessor(
            launchDataWrapper,
            operationContextFactory.getOperationContext())
            .execute();
    }

    public LaunchDataWrapper getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public BaseOperationContextFactory<C> getOperationContextFactory()
    {
        return operationContextFactory;
    }

    public void setOperationContextFactory(BaseOperationContextFactory<C> operationContextFactory)
    {
        this.operationContextFactory = operationContextFactory;
    }
}
