package com.theplatform.commandbuilder.impl.build.handler.connect;

import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectionKeys;

public class NoopConnectKey implements ConnectionKeys
{
    public static final String NOOP_CONNECT_KEY = "NOOP_CONNECT_KEY";
    @Override
    public String name()
    {
        return NOOP_CONNECT_KEY;
    }
}
