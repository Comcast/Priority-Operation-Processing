package com.theplatform.dfh.cp.handler.puller.impl.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class PullerConfig extends Configuration
{
    private String agendaProviderUrl;
    private String identityUrl;
    private String username;
    private String encryptedPassword;
    private int pullWait;
    private String insightId;
    private int agendaRequestCount=1;

    public String getAgendaProviderUrl()
    {
        return agendaProviderUrl;
    }

    public PullerConfig setAgendaProviderUrl(String agendaProviderUrl)
    {
        this.agendaProviderUrl = agendaProviderUrl;
        return this;
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
}
