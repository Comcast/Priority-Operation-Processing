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

        AuthorizationResponse resp = new AuthorizationResponseBuilder().withAccounts("http://access.auth.test.corp.theplatform.com/data/Account/3131523765|http://access.auth" +
            ".test.corp.theplatform.com/data/Account/3131523999").build();
        System.out.println(resp);
    }
}
