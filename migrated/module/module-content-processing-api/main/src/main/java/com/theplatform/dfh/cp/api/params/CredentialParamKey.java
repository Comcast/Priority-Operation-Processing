package com.theplatform.dfh.cp.api.params;

public enum CredentialParamKey implements ParamKey
{
    username, password;

    public String getKey()
    {
        return this.name();
    }
}
