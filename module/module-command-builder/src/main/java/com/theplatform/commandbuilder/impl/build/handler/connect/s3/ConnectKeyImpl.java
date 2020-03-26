package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectionKeys;

public class ConnectKeyImpl implements ConnectionKeys
{
    private final String name;

    public ConnectKeyImpl(String name)
    {
        this.name = name;
    }

    @Override
    public String name()
    {
        return name;
    }
}
