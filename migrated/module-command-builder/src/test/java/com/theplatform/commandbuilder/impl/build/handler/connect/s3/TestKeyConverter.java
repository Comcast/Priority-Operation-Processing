package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectionKeys;
import com.theplatform.commandbuilder.impl.build.handler.connect.KeyConversion;
import com.theplatform.commandbuilder.impl.build.handler.connect.NoopConnectKey;

import java.util.Arrays;
import java.util.Optional;

public class TestKeyConverter implements KeyConversion
{
    private static ConnectionKeys notFound = new NoopConnectKey();

    public ConnectionKeys convertKey(String keyToConvert)
    {
        Optional<ConnectionKeys> optionalKey = Arrays.stream(TestKeyMap.values())
                .filter(converter -> converter.name().equals(keyToConvert))
                .map(TestKeyMap::getKey).findFirst();
        return optionalKey.orElseGet(() -> notFound);
    }
}

