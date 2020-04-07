package com.comast.pop.handler.base.context;

import com.comast.pop.handler.base.field.api.HandlerField;
import com.comast.pop.handler.base.field.api.LaunchType;
import com.comast.pop.handler.base.field.api.args.HandlerArgument;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public abstract class BaseOperationContextFactory<T extends BaseOperationContext>
{
    private final static Logger logger = LoggerFactory.getLogger(BaseOperationContext.class);
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

    public T create()
    {
        T context = createOperationContext();
        context.setCid(retrieveCID());
        return context;
    }

    public abstract T createOperationContext();

    public String retrieveCID()
    {
        String cid = launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.CID.name(), null);
        if (cid == null)
        {
            cid = UUID.randomUUID().toString();
            logger.warn(
                String.format("%1$s was not set in the environment. Generated: %2$s",
                    HandlerField.CID.name(), cid));
        }
        return cid;
    }
}
