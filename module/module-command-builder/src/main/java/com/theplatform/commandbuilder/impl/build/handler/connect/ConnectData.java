package com.theplatform.commandbuilder.impl.build.handler.connect;

import java.util.Map;

public interface ConnectData extends Connect
{
    Map<String, String> getParameters();
    void setPrivilege(boolean privilege);
}
