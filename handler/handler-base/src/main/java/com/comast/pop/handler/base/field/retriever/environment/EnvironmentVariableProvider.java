package com.comast.pop.handler.base.field.retriever.environment;

/**
 * This intermediate is pretty much just for the sake of unit tests.
 */
public class EnvironmentVariableProvider
{
    public String getVariable(String envVar)
    {
        return System.getenv(envVar);
    }
}
