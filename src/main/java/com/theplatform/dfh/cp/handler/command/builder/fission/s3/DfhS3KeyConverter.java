package com.theplatform.dfh.cp.handler.command.builder.fission.s3;

import com.theplatform.commandbuilder.impl.build.handler.ConnectionKeys;
import com.theplatform.commandbuilder.impl.build.handler.KeyConversion;
import com.theplatform.commandbuilder.impl.build.handler.NoopConnectKey;

import java.util.Arrays;
import java.util.Optional;

public class DfhS3KeyConverter implements KeyConversion
{
    private static final ConnectionKeys notFound = new NoopConnectKey();

    public ConnectionKeys convertKey(String keyToConvert)
    {
        Optional<ConnectionKeys> optionalKey = Arrays.stream(S3ConnectConvert.values())
                .filter(converter -> converter.name().equals(keyToConvert))
                .map(S3ConnectConvert::getKey).findFirst();
        return optionalKey.orElseGet(() -> notFound);
    }
}
