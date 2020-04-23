package com.comcast.pop.handler.kubernetes.support.config;

import com.comast.pop.handler.base.field.retriever.api.NamedField;

public enum KubeConfigField implements NamedField
{
    MASTER_URL("pop.kubernetes.masterUrl"),
    NAMESPACE("pop.kubernetes.namespace"),
    ZONE("pop.kubernetes.zone");

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
