package com.theplatform.dfh.cp.api.source;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;

public class Source
{
    /**
     * To reference this Source in other API parts
     */
    private String label;

    /**
     * Path to this Source, using file protocols supported by DFH File Handlers (initially, just mount paths)
     */
    private String url;
    /**
     * Username: Credentials to access the source
     */
    private String username;
    /**
     * Password: Credentials to access the source
     */
    private String password;

    @JsonProperty
    public String getLabel()
    {
        return label;
    }

    @JsonProperty
    public void setLabel(String label)
    {
        this.label = label;
    }

    @JsonProperty
    public String getUrl()
    {
        return url;
    }

    @JsonProperty
    public void setUrl(String url)
    {
        this.url = url;
    }

    @JsonProperty
    public String getUsername()
    {
        return username;
    }

    @JsonProperty
    public void setUsername(String username)
    {
        this.username = username;
    }

    @JsonProperty
    public String getPassword()
    {
        return password;
    }

    @JsonProperty
    public void setPassword(String password)
    {
        this.password = password;
    }
}
