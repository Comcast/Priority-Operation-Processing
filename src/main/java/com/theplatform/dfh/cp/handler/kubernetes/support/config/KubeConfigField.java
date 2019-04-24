package com.theplatform.dfh.cp.handler.kubernetes.support.config;

public enum KubeConfigField
{
    MASTER_URL("cp.kubernetes.masterUrl"),
    NAMESPACE("cp.kubernetes.namespace");

    private final String fieldName;

    KubeConfigField(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getFieldName()
    {
        return fieldName;
    }
}
