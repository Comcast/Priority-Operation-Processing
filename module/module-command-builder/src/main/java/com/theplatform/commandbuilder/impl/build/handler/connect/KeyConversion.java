package com.theplatform.commandbuilder.impl.build.handler.connect;

import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectionKeys;

public interface KeyConversion
{
    ConnectionKeys convertKey(String keyToConvert);
}
