package com.theplatform.dfh.endpoint.api.auth;

import java.util.Collections;

public class CustomerIdAuthorizationResponse extends AuthorizationResponse

{
    public CustomerIdAuthorizationResponse(String customerID)
    {
        super(null, null, Collections.singleton(customerID), DataVisibility.authorized_account);
    }
}
