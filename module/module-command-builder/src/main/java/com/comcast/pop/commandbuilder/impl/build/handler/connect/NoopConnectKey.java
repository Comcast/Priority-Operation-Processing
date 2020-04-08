package com.comcast.pop.commandbuilder.impl.build.handler.connect;

public class NoopConnectKey implements ConnectionKeys
{
    public static final String NOOP_CONNECT_KEY = "NOOP_CONNECT_KEY";
    @Override
    public String name()
    {
        return NOOP_CONNECT_KEY;
    }
}
