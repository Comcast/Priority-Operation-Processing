package com.theplatform.dfh.cp.handler.base.field.api.args;

public enum HandlerArgument implements CommandLineArgument
{
    LAUNCH_TYPE("launchType"),
    EXTERNAL_LAUNCH_TYPE("externalLaunchType"),
    PROP_FILE("propFile"),
    PAYLOAD_FILE("payloadFile"),
    LAST_OPERATION_PROGRESS_FILE("lastOpProgressFile");

    private final String argumentName;

    HandlerArgument(String argumentName)
    {
        this.argumentName = argumentName;
    }

    @Override
    public String getArgumentName()
    {
        return argumentName;
    }
}
