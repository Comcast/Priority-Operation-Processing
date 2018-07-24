package com.theplatform.dfh.cp.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Credentials
{
    private String username;
    private String password;

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
