package com.comcast.pop.endpoint.api.auth;

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

        AuthorizationResponse resp = new AuthorizationResponseBuilder().withAccounts("http://myaccounturl/data/Account/3131523765|http://access.auth" +
            ".com/data/Account/3131523999").build();
        System.out.println(resp);
    }
}
