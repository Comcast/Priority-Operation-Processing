package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import com.theplatform.commandbuilder.impl.command.api.CommandGeneratorBuilder;
import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectData;
import com.theplatform.commandbuilder.impl.build.handler.connect.KeyConversion;

import java.util.Optional;

public class S3CommandGeneratorBuilder implements CommandGeneratorBuilder<ConnectGenerator, ConnectData>
{

    private S3Connect s3Connect;
    private final KeyConversion keyConverter;

    public S3CommandGeneratorBuilder(KeyConversion keyConverter)
    {
        this.keyConverter = keyConverter;
    }

    @Override
    public boolean isType(ConnectData input)
    {
        return  input.getUrl() !=null && S3TypeUtil.isParsable(input.getUrl());
    }

    @Override
    public Optional<ConnectGenerator> makeCommandGenerator(ConnectData input)
    {
        Optional<ConnectGenerator> commandGeneratorContainer = Optional.ofNullable(null);
        if(isType(input))
        {
            s3Connect = new S3VhsConnect(keyConverter);
            ConnectGenerator connectGenerator = new  S3CommandGenerator(s3Connect.makeS3Data(input), s3Connect);
            commandGeneratorContainer = Optional.ofNullable(connectGenerator);
        }
        return commandGeneratorContainer;
    }
}
