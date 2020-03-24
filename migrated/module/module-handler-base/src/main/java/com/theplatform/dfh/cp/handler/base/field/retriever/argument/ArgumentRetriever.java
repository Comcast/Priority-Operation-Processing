package com.theplatform.dfh.cp.handler.base.field.retriever.argument;

import com.theplatform.dfh.cp.handler.base.field.retriever.api.FieldRetriever;

public class ArgumentRetriever extends FieldRetriever
{
    private ArgumentProvider argumentProvider;

    public ArgumentRetriever(ArgumentProvider argumentProvider)
    {
        this.argumentProvider = argumentProvider;
    }

    @Override
    public String getField(String field)
    {
        return argumentProvider.getArgument(field);
    }

    @Override
    public String getField(String field, String defaultValue)
    {
        return argumentProvider.getArgument(field, defaultValue);
    }

    @Override
    public boolean isFieldSet(String field)
    {
        return false;
    }

    public ArgumentProvider getArgumentProvider()
    {
        return argumentProvider;
    }

    public void setArgumentProvider(ArgumentProvider argumentProvider)
    {
        this.argumentProvider = argumentProvider;
    }
}
