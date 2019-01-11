package com.theplatform.dfh.endpoint.api;

import java.util.HashSet;
import java.util.Set;

public class AuthorizationResponse
{
    // the current user ID
    private String userID;

    // the current user name
    private String userName;

    // allowed accounts
    private Set<String> allowedCustomerIds = new HashSet<>();

    // whether access is unlimited
    private boolean isSuperUser;

    public AuthorizationResponse(String userID, String userName, Set<String> allowedCustomerIds, boolean isSuperUser)
    {
        this.userID = userID;
        this.userName = userName;
        this.allowedCustomerIds = allowedCustomerIds;
        this.isSuperUser = isSuperUser;
    }

    public String getUserID()
    {
        return userID;
    }

    public String getUserName()
    {
        return userName;
    }

    public Set<String> getAllowedCustomerIds()
    {
        return allowedCustomerIds;
    }

    public boolean isSuperUser()
    {
        return isSuperUser;
    }
}
