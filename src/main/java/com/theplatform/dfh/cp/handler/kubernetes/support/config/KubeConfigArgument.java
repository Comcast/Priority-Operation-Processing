package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.handler.field.retriever.api.NamedField;

public enum KubeConfigArgument implements NamedField
{
    OAUTH_CERT_FILE_PATH("oauthCertPath"),
    OAUTH_TOKEN_FILE_PATH("oauthTokenPath");

    private final String fieldName;

    KubeConfigArgument(String fieldName)
    {
        this.fieldName = fieldName;
    }

    @Override
    public String getFieldName()
    {
        return fieldName;
    }
}
