package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.handler.base.field.retriever.api.NamedField;

public enum KubeConfigField implements NamedField
{
    MASTER_URL("cp.kubernetes.masterUrl"),
    NAMESPACE("cp.kubernetes.namespace"),
    ZONE("cp.kubernetes.zone");

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
