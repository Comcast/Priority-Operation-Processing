package com.theplatform.commandbuilder.impl.build.handler.exception;

public class HandlerCommandException extends RuntimeException
{
    public HandlerCommandException(String commandErrorMessage)
    {
        super(commandErrorMessage);
    }
}
