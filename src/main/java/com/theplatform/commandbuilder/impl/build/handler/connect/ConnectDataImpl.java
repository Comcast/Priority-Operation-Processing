package com.theplatform.commandbuilder.impl.build.handler.connect;

import java.util.HashMap;
import java.util.Map;

public class ConnectDataImpl implements ConnectData
{
    private final String url;
    private final Map<String, String> parameters;
    private boolean needsPrivilege = false; // default

    public ConnectDataImpl(String url, Map<String, String> parameters)
    {
        this.url = url;
        this.parameters = parameters == null ? new HashMap<>() : parameters;
    }

    @Override
    public String getUrl()
    {
        return url;
    }

    @Override
    public boolean needsPrivilege()
    {
        return needsPrivilege;
    }

    @Override
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    @Override
    public void setPrivilege(boolean privilege)
    {
        this.needsPrivilege = privilege;
    }
}

