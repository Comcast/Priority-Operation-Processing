package com.theplatform.dfh.cp.handler.base.context;

import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

public abstract class BaseOperationContext<T extends LaunchDataWrapper>
{
    private T launchDataWrapper;

    public BaseOperationContext(T launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public void init(){}
    public void shutdown(){}

    public T getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public void setLaunchDataWrapper(T launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    /**
     * Processes an exception that was unhandled by the implementation of the handler
     * @param message A message related to the unhandled exception.
     * @param e The exception to process
     */
    public abstract void processUnhandledException(String message, Exception e);
}
