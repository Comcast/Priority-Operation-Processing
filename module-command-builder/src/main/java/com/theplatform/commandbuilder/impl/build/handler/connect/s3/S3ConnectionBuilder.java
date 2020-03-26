package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectData;
import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectionBuilder;
import com.theplatform.commandbuilder.impl.build.handler.exception.HandlerCommandException;
import com.theplatform.commandbuilder.impl.build.handler.connect.KeyConversion;

public class S3ConnectionBuilder implements ConnectionBuilder<S3Data>
{
    private S3VhsConnect s3VhsConnect;
    private final KeyConversion keyConversion;

    public S3ConnectionBuilder(KeyConversion keyConversion)
    {
        this.keyConversion = keyConversion;
    }


    @Override
    public S3Data build(ConnectData connectData)
    {
        if(!isType(connectData))
        {
            throw new HandlerCommandException("ConnectData instance does not convert to S3Data; ConnectData url: " + connectData.getUrl());
        }
            s3VhsConnect = new S3VhsConnect(keyConversion);
        return s3VhsConnect.makeS3Data(connectData);
    }

    @Override
    public boolean isType(ConnectData connectData)
    {
        return S3TypeUtil.isParsable(connectData.getUrl());
    }
}
