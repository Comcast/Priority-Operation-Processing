package com.comcast.pop.api.params;

public enum FileParamKey implements ParamKey
{
    extension,
    externalFileId,
    fileSize,
    ldapOriginalPrefix,
    ldapRemappedPrefix;

    public String getKey()
    {
        return this.name();
    }
}
