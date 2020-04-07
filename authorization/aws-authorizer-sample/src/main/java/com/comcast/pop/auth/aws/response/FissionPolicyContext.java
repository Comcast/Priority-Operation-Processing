package com.comcast.pop.auth.aws.response;

import org.apache.commons.lang3.StringUtils;

public class FissionPolicyContext extends PolicyContext
{
    private static final String ENV_VAR_FISSION_USER_ID = "FISSION_USER_ID";
    private static final String ENV_VAR_FISSION_USER = "FISSION_USER";
    private static final String ENV_VAR_FISSION_ACCOUNTS = "FISSION_ACCOUNTS";

    // the account wildcard
    private static final String ACCOUNT_WILDCARD = "*";

    public FissionPolicyContext()
    {
        super();
        addAuthResponse();
    }

    private void addAuthResponse()
    {
        // TODO: move to constants to be shared with the LambdaRequest class on the endpoint side of things

        String accounts = retrieveAccounts();

        put("userId", retrieveUserId());
        final boolean isSuperUser = isSuperUser(accounts);
        if(isSuperUser)
        {
            put("accounts", "");
        }
        else
        {
            put("accounts", accounts);
        }
        put("isSuperUser", String.valueOf(isSuperUser));
        put("userName", retrieveUser());
    }

    public String retrieveUserId()
    {
        return retrieveEnvVar(ENV_VAR_FISSION_USER_ID, "");
    }

    private String retrieveUser()
    {
        return retrieveEnvVar(ENV_VAR_FISSION_USER, "");
    }

    private String retrieveAccounts()
    {
        return retrieveEnvVar(ENV_VAR_FISSION_ACCOUNTS, "");
    }

    private String retrieveEnvVar(String var, String defaultValue)
    {
        String accounts = System.getenv(var);
        return accounts == null ? defaultValue : accounts;
    }

    private boolean isSuperUser(String accounts)
    {
        return StringUtils.startsWithIgnoreCase(accounts, ACCOUNT_WILDCARD);
    }
}
