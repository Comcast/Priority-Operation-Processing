package com.comcast.pop.commandbuilder.impl.build.handler.connect;

public class NoopConnection implements Connect
{
    public static String NOOP_URL = "file://noop.url";
    private String url;

    public NoopConnection(String url)
    {

        this.url = url;
    }

    @Override
    public String getUrl()
    {
        return url == null ? NOOP_URL : url;
    }

    @Override
    public boolean needsPrivilege()
    {
        return false;
    }
}
