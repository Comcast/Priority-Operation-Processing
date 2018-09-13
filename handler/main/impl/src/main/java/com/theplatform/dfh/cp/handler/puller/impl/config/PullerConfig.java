package com.theplatform.dfh.cp.handler.puller.impl.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class PullerConfig extends Configuration
{
    private String agendaProviderUrl;
    private String identityUrl;
    private String username;
    private String encryptedPassword;

    public String getAgendaProviderUrl()
    {
        return agendaProviderUrl;
    }

    public PullerConfig setAgendaProviderUrl(String agendaProviderUrl)
    {
        this.agendaProviderUrl = agendaProviderUrl;
        return this;
    }

    @JsonProperty
    public String getIdentityUrl()
    {
        return identityUrl;
    }

    @JsonProperty
    public PullerConfig setIdentityUrl(String identityUrl)
    {
        this.identityUrl = identityUrl;
        return this;
    }

    @JsonProperty
    public String getUsername()
    {
        return username;
    }

    @JsonProperty
    public PullerConfig setUsername(String username)
    {
        this.username = username;
        return this;
    }

    @JsonProperty
    public String getEncryptedPassword()
    {
        return encryptedPassword;
    }

    @JsonProperty
    public PullerConfig setEncryptedPassword(String encryptedPassword)
    {
        this.encryptedPassword = encryptedPassword;
        return this;
    }
}
