package com.theplatform.dfh.http.api;

public abstract class HTTPClientConfig
{
    private String proxyHost;
    private String proxyPort;

    public abstract String getProviderUrl();

    public String getProxyHost()
    {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost)
    {
        this.proxyHost = proxyHost;
    }

    public String getProxyPort()
    {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort)
    {
        this.proxyPort = proxyPort;
    }
}
