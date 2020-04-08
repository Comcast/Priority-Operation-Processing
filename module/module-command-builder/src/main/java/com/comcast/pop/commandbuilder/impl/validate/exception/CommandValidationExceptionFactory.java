package com.comcast.pop.commandbuilder.impl.validate.exception;

import com.comcast.pop.commandbuilder.api.CommandExceptionFactory;

public class CommandValidationExceptionFactory implements CommandExceptionFactory<CommandValidationException> {

    private String errorPrefix;

    public CommandValidationExceptionFactory(String prefix) {
        errorPrefix = prefix;
    }

    @Override
    public void setErrorPrefix(String prefix)
    {
        errorPrefix = prefix;
    }

    @Override
    public CommandValidationException makeException(String suffix) {
        return new CommandValidationException(errorPrefix + ": " + suffix);
    }
}
