package com.theplatform.dfh.endpoint.api.resourcepool.service;

import com.theplatform.dfh.endpoint.api.auth.AuthorizationResponse;
import com.theplatform.dfh.endpoint.api.auth.DataVisibility;

import java.util.Collections;

public class CustomerIdAuthorizationResponse extends AuthorizationResponse

{
    public CustomerIdAuthorizationResponse(String customerID)
    {
        super(null, null, Collections.singleton(customerID), DataVisibility.authorized_account);
    }
}
