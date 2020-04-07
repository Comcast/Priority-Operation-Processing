package com.comcast.pop.handler.kubernetes.support.config;

import com.comast.pop.handler.base.field.retriever.api.NamedField;

public enum KubeConfigField implements NamedField
{
    MASTER_URL("fission.kubernetes.masterUrl"),
    NAMESPACE("fission.kubernetes.namespace"),
    ZONE("fission.kubernetes.zone");

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
