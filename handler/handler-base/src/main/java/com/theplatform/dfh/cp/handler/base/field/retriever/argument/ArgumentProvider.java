package com.theplatform.dfh.cp.handler.base.field.retriever.argument;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public abstract class ArgumentProvider
{
    protected CommandLine commandLine;

    public ArgumentProvider(String[] args)
    {
        try
        {
            commandLine = new DefaultParser().parse(getOptions(), args);
        }
        catch(ParseException e)
        {
            throw new RuntimeException("Error parsing handling args.", e);
        }
    }

    protected abstract Options getOptions();

    public abstract String getArgument(String argumentName);

    public abstract String getArgument(String argumentName, String defaultValue);

    public boolean isArgumentSet(String argumentName)
    {
        return commandLine.hasOption(argumentName);
    }
}