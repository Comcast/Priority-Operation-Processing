package com.theplatform.dfh.endpoint.api.auth;

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
    private DataVisibility visibility;


    public AuthorizationResponse(String userID, String userName, Set<String> allowedCustomerIds, DataVisibility visibility)
    {
        this.userID = userID;
        this.userName = userName;
        if(allowedCustomerIds != null)
            this.allowedCustomerIds = allowedCustomerIds;
        this.visibility = visibility;
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

    public DataVisibility getVisibility()
    {
        return visibility;
    }

}
