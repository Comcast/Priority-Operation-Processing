package com.comcast.pop.commandbuilder.api;

public interface CommandExceptionFactory<T extends RuntimeException>
{
    void setErrorPrefix(String prefix);

    T makeException(String suffix);
}
