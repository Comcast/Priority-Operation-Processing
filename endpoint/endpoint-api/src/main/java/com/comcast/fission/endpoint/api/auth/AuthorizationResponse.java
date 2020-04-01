package com.comcast.fission.endpoint.api.auth;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("{");
        builder.append("\"userId\":\"").append(userID).append("\",");
        builder.append("\"userName\":\"").append(userName).append("\",");
        builder.append("\"visibility\":\"").append(visibility).append("\",");
        builder.append("\"accounts\":\"").append(delimitAccounts()).append("\"}");
        return builder.toString();
    }
    private String delimitAccounts()
    {
        if(allowedCustomerIds != null)
        {
            return allowedCustomerIds.stream().collect(Collectors.joining("|"));
        }
        return null;
    }
}
