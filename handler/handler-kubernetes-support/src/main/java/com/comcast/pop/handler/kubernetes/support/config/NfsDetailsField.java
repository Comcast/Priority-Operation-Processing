package com.comcast.pop.handler.kubernetes.support.config;

public enum NfsDetailsField
{
    nfsServerPath("serverPath"),
    nfsReadOnly("readOnly"),
    nfsMountPaths("mountPaths"),
    nfsServer("server");

    private final String fieldName;

    NfsDetailsField(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getFieldName()
    {
        return fieldName;
    }
}
