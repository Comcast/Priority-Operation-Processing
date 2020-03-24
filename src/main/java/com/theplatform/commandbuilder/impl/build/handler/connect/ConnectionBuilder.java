package com.theplatform.commandbuilder.impl.build.handler.connect;

public interface ConnectionBuilder<T extends Connect>
{
    T build(ConnectData connectData);
    boolean isType(ConnectData connectData);
}
