package com.theplatform.dfh.cp.handler.command.builder.fission.s3;

import com.theplatform.commandbuilder.impl.build.handler.ConnectionKeys;
import com.theplatform.commandbuilder.impl.build.handler.s3.S3ConnectionKeys;

public enum S3ConnectConvert
{
    username(S3ConnectionKeys.ID),
    password(S3ConnectionKeys.SECRET),
    mount(S3ConnectionKeys.MOUNT);

    private ConnectionKeys key;

    S3ConnectConvert(ConnectionKeys key)
    {
        this.key = key;
    }

    ConnectionKeys getKey()
    {
        return key;
    }
}
