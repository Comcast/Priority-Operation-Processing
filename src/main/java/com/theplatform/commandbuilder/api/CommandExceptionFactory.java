package com.theplatform.commandbuilder.api;

public interface CommandExceptionFactory<T extends RuntimeException>
{
    void setErrorPrefix(String prefix);

    T makeException(String suffix);
}
