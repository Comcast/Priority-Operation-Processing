package com.theplatform.dfh.cp.handler.base.context;

import com.theplatform.dfh.cp.handler.field.api.LaunchType;
import com.theplatform.dfh.cp.handler.field.api.args.HandlerArgument;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

public abstract class BaseOperationContextFactory<T extends BaseOperationContext>
{
    protected LaunchDataWrapper launchDataWrapper;

    public BaseOperationContextFactory(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public LaunchType getLaunchType()
    {
        return getLaunchType(HandlerArgument.LAUNCH_TYPE, LaunchType.kubernetes);
    }

    public LaunchType getExternalLaunchType()
    {
        return getLaunchType(HandlerArgument.EXTERNAL_LAUNCH_TYPE, LaunchType.kubernetes);
    }

    protected LaunchType getLaunchType(HandlerArgument handlerArgument, LaunchType defaultLaunchType)
    {
        try
        {
            return Enum.valueOf(LaunchType.class,
                launchDataWrapper.getArgumentRetriever().getField(handlerArgument.getArgumentName(), defaultLaunchType.name()));
        }
        catch(Exception e)
        {
            throw new RuntimeException(String.format("Failed to evaluate %1$s", handlerArgument.getArgumentName()), e);
        }
    }

    public LaunchDataWrapper getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public abstract T getOperationContext();
}
