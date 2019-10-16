package com.theplatform.dfh.cp.api.tokens;

public enum AgendaToken
{
    AGENDA_ID("agendaId"),
    OPERATION_NAME("operationName")
    ;

    public static final String TOKEN_PREFIX = "fission.";
    private final String tokenSuffix;

    public String getToken()
    {
        return TOKEN_PREFIX + tokenSuffix;
    }

    AgendaToken(String tokenSuffix)
    {
        this.tokenSuffix = tokenSuffix;
    }
}
