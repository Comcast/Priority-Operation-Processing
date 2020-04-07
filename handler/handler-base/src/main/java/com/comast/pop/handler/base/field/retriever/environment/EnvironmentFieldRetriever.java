package com.comast.pop.handler.base.field.retriever.environment;

import com.comast.pop.handler.base.field.retriever.api.FieldRetriever;
import org.apache.commons.lang3.StringUtils;

public class EnvironmentFieldRetriever extends FieldRetriever
{
    private EnvironmentVariableProvider environmentVariableProvider;

    public EnvironmentFieldRetriever()
    {
        environmentVariableProvider = new EnvironmentVariableProvider();
    }

    @Override
    public String getField(String field)
    {
        return getEnvironmentVariableValue(field, null);
    }

    @Override
    public String getField(String field, String defaultValue)
    {
        return getEnvironmentVariableValue(field, defaultValue);
    }

    @Override
    public boolean isFieldSet(String field)
    {
        return getEnvironmentVariableValue(field, null) != null;
    }

    String getEnvironmentVariableValue(String envVar, String defaultValue)
    {
        String value = environmentVariableProvider.getVariable(envVar);
        if(StringUtils.isEmpty(value))
        {
            value = defaultValue;
        }
        return value;
    }

    public void setEnvironmentVariableProvider(EnvironmentVariableProvider environmentVariableProvider)
    {
        this.environmentVariableProvider = environmentVariableProvider;
    }
}
