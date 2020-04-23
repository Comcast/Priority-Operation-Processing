package com.comcast.pop.api.params;

public enum CredentialParamKey implements ParamKey
{
    username, password;

    public String getKey()
    {
        return this.name();
    }
}
