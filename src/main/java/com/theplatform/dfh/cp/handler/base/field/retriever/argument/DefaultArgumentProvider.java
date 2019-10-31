package com.theplatform.dfh.cp.handler.base.field.retriever.argument;

import com.theplatform.dfh.cp.handler.base.field.api.args.HandlerArgument;
import org.apache.commons.cli.Options;

/**
 * Argument provider with standard command line options provided.
 */
public class DefaultArgumentProvider extends ArgumentProvider
{
    public DefaultArgumentProvider(String[] args)
    {
        super(args);
    }

    @Override
    protected Options getOptions()
    {
        Options options = new Options();

        // THIS IS A HACK - The containers are built to launch the java main with a number of arguments (including -c)
        options.addOption("c", false, "THIS IS GARBAGE");

        addOption(options, HandlerArgument.LAUNCH_TYPE, true, "Indicator of how this handler was launched.");
        addOption(options, HandlerArgument.EXTERNAL_LAUNCH_TYPE, true, "The type of launcher to use for external processes.");
        addOption(options, HandlerArgument.PROP_FILE, true, "The path to the properties file to use. (This overrides any other setting)");
        addOption(options, HandlerArgument.PAYLOAD_FILE, true, "The path to payload file to use. This overrides the environment variable.");

        return options;
    }

    @Override
    public String getArgument(String argumentName)
    {
        return getArgument(argumentName, null);
    }

    @Override
    public String getArgument(String argumentName, String defaultValue)
    {
        return commandLine.getOptionValue(argumentName, defaultValue);
    }

    protected void addOption(Options options, HandlerArgument handlerArgument, boolean hasArg, String description)
    {
        addOption(options, handlerArgument.getArgumentName(), hasArg, description);
    }

    protected void addOption(Options options, String handlerArgument, boolean hasArg, String description)
    {
        options.addOption(handlerArgument, hasArg, description);
    }
}
