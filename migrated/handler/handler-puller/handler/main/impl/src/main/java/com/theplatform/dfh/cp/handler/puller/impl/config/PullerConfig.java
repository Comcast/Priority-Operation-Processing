package com.theplatform.dfh.cp.handler.puller.impl.config;

import com.theplatform.dfh.cp.handler.puller.impl.dropwizard.LogbackAutoConfigLoggingFactory;
import io.dropwizard.Configuration;
import io.dropwizard.logging.LoggingFactory;

public class PullerConfig extends Configuration
{
    private String agendaProviderUrl;
    private String identityUrl;
    private String username;
    private String encryptedPassword;
    private int pullWait;
    private String insightId;
    private int agendaRequestCount=1;
    private String localAgendaRelativePath;
    private String proxyHost;
    private String proxyPort;

    public String getAgendaProviderUrl()
    {
        return agendaProviderUrl;
    }

    public PullerConfig setAgendaProviderUrl(String agendaProviderUrl)
    {
        this.agendaProviderUrl = agendaProviderUrl;
        return this;
    }

    public String getLocalAgendaRelativePath()
    {
        return localAgendaRelativePath;
    }

    public PullerConfig setLocalAgendaRelativePath(String localAgendaRelativePath)
    {
        this.localAgendaRelativePath = localAgendaRelativePath;
        return this;
    }

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

    public String getIdentityUrl()
    {
        return identityUrl;
    }

    public PullerConfig setIdentityUrl(String identityUrl)
    {
        this.identityUrl = identityUrl;
        return this;
    }

    public String getUsername()
    {
        return username;
    }

    public PullerConfig setUsername(String username)
    {
        this.username = username;
        return this;
    }

    public String getEncryptedPassword()
    {
        return encryptedPassword;
    }

    public PullerConfig setEncryptedPassword(String encryptedPassword)
    {
        this.encryptedPassword = encryptedPassword;
        return this;
    }

    public int getPullWait()
    {
        return pullWait;
    }

    public PullerConfig setPullWait(int pullWait)
    {
        this.pullWait = pullWait;
        return this;
    }

    public boolean useInsights()
    {
        return insightId != null && insightId.length() > 0;
    }

    public String getInsightId()
    {
        return insightId;
    }

    public PullerConfig setInsightId(String insightId)
    {
        this.insightId = insightId;
        return this;
    }

    public int getAgendaRequestCount()
    {
        return agendaRequestCount;
    }

    public PullerConfig setAgendaRequestCount(int agendaRequestCount)
    {
        this.agendaRequestCount = agendaRequestCount;
        return this;
    }

    @Override
    public synchronized LoggingFactory getLoggingFactory()
    {
        // This custom factory allows drop wizard to use the logback.xml (by default it only uses the config yaml)
        return new LogbackAutoConfigLoggingFactory();
    }
}
