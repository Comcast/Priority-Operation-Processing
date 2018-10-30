package com.theplatform.dfh.cp.handler.base;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

public abstract class BaseHandlerEntryPoint<C extends BaseOperationContext, P extends HandlerProcessor, W extends LaunchDataWrapper>
{
    private W launchDataWrapper;
    private BaseOperationContextFactory<C> operationContextFactory;

    protected abstract W createLaunchDataWrapper(String[] args);
    protected abstract BaseOperationContextFactory<C> createOperationContextFactory(W launchDataWrapper);
    protected abstract P createHandlerProcessor(W launchDataWrapper, C operationContext);

    public BaseHandlerEntryPoint(String[] args)
    {
        // gather the inputs args, environment, properties
        launchDataWrapper = createLaunchDataWrapper(args);
        operationContextFactory = createOperationContextFactory(launchDataWrapper);
    }

    public void execute()
    {
        // get the operation specific context for running the overall process
        C operationContext = operationContextFactory.createOperationContext();
        try
        {
            operationContext.init();
            createHandlerProcessor(launchDataWrapper, operationContext).execute();
        }
        finally
        {
            operationContext.shutdown();
        }
    }

    public W getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public void setLaunchDataWrapper(W launchDataWrapper)
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
