package com.theplatform.dfh.endpoint.api.auth;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

public class AuthorizationResponseTest
{
    @Test
    public void testToString()
    {
        AuthorizationResponse authorizationResponse = new AuthorizationResponse("myUserId", "myUserName", Collections.singleton("myAccountId"),
            DataVisibility.authorized_account);
        Assert.assertNotNull(authorizationResponse.toString());
    }
}
