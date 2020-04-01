package com.comcast.fission.endpoint.api.auth;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AuthorizationResponseBuilder
{
    private static final String DELIMITER = "\\|";
    private String userId;
    private String username;
    private DataVisibility dataVisibility = DataVisibility.authorized_account;
    private Set<String> accountList = new HashSet<>();

    public AuthorizationResponseBuilder withUserId(String userId)
    {
        this.userId = userId;
        return this;
    }
    public AuthorizationResponseBuilder withUsername(String username)
    {
        this.username = username;
        return this;
    }
    public AuthorizationResponseBuilder withAccounts(String accounts)
    {
        // TODO: share the * constant
        parseAccounts(accounts);
        if(dataVisibility == DataVisibility.authorized_account && accountList.contains("*"))
            dataVisibility = DataVisibility.global;
        return this;
    }
    public AuthorizationResponseBuilder withSuperUser(String isSuperUser)
    {
        if(isSuperUser == null) return this;
        boolean isSuperUserBool = Boolean.parseBoolean(isSuperUser);
        return withSuperUser(isSuperUserBool);
    }
    public AuthorizationResponseBuilder withSuperUser(boolean isSuperUser)
    {
        if(isSuperUser)
            dataVisibility = DataVisibility.global;
        return this;
    }
    private void parseAccounts(String accounts)
    {
        if(accounts == null) return;
        String[] values = accounts.split(DELIMITER);
        this.accountList.addAll(Arrays.asList(values));
    }

    public AuthorizationResponse build()
    {
        return new AuthorizationResponse(userId, username, accountList, dataVisibility);
    }
}
