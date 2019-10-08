package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectionKeys;

public enum TestKeyMap
{
    username(S3ConnectionKeys.ID),
    password(S3ConnectionKeys.SECRET),
    mount(S3ConnectionKeys.MOUNT);

    private ConnectionKeys key;

    TestKeyMap(ConnectionKeys key)
    {
        this.key = key;
    }

    ConnectionKeys getKey()
    {
        return key;
    }
}