package com.theplatform.commandbuilder.impl.build.handler.connect;

public class NoopConnectionBuilder implements ConnectionBuilder<NoopConnection>
{
    @Override
    public NoopConnection build(ConnectData connectData)
    {
        return new NoopConnection(connectData.getUrl());
    }

    @Override
    public boolean isType(ConnectData connectData)
    {
        return false;
    }
}
