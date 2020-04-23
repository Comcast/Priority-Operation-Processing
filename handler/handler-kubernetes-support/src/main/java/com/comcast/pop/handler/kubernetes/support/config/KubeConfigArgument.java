package com.comcast.pop.handler.kubernetes.support.config;

import com.comast.pop.handler.base.field.retriever.api.NamedField;

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
