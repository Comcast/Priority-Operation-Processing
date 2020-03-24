package com.theplatform.dfh.cp.handler.base.field.retriever;

import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.base.field.retriever.argument.ArgumentRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.environment.EnvironmentFieldRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.properties.PropertyRetriever;

/**
 * Provides access to the 3 main sources of inputs: args, environment variables, and properties (from a properties file)
 */
public abstract class LaunchDataWrapper
{
    private ArgumentRetriever argumentRetriever;
    private EnvironmentFieldRetriever environmentRetriever;
    private PropertyRetriever propertyRetriever;

    public ArgumentRetriever getArgumentRetriever()
    {
        return argumentRetriever;
    }

    public void setArgumentRetriever(ArgumentRetriever argumentRetriever)
    {
        this.argumentRetriever = argumentRetriever;
    }

    public EnvironmentFieldRetriever getEnvironmentRetriever()
    {
        return environmentRetriever;
    }

    public void setEnvironmentRetriever(EnvironmentFieldRetriever environmentRetriever)
    {
        this.environmentRetriever = environmentRetriever;
    }

    public PropertyRetriever getPropertyRetriever()
    {
        return propertyRetriever;
    }

    public void setPropertyRetriever(PropertyRetriever propertyRetriever)
    {
        this.propertyRetriever = propertyRetriever;
    }

    public abstract String getPayload();

    public OperationProgress getLastOperationProgress() { return null; }

    public <T> T getLastProgressObject(Class<T> objectClass) { return null; }

    public <T> T getLastOperationProgressParam(String paramName, Class<T> objectClass) { return null; }

    // TODO: consider field accessors for getting the payload, launch type, and other extremely common fields (maybe a DefaultLaunchDataWrapper)
}
