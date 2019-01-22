package com.theplatform.dfh.endpoint.api.auth;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MPXAuthorizationResponseBuilder
{
    private static final String DELIMITER = "|";
    private String userId;
    private String username;
    private DataVisibility dataVisibility = DataVisibility.authorized_account;
    private Set<String> accountList = new HashSet<>();

    public MPXAuthorizationResponseBuilder withUserId(String userId)
    {
        this.userId = userId;
        return this;
    }
    public MPXAuthorizationResponseBuilder withUsername(String username)
    {
        this.username = username;
        return this;
    }
    public MPXAuthorizationResponseBuilder withAccounts(String accounts)
    {
        parseAccounts(accounts);
        if(dataVisibility == DataVisibility.authorized_account && accountList.contains("urn:theplatform:auth:any"))
            dataVisibility = DataVisibility.global;
        return this;
    }
    public MPXAuthorizationResponseBuilder withSuperUser(String isSuperUser)
    {
        if(isSuperUser == null) return this;
        boolean isSuperUserBool = Boolean.getBoolean(isSuperUser);
        return withSuperUser(isSuperUserBool);
    }
    public MPXAuthorizationResponseBuilder withSuperUser(boolean isSuperUser)
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
