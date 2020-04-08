package com.comcast.pop.auth.aws.response;

import org.apache.commons.lang.StringUtils;

public class AuthorizerRequest
{
    private String awsAccountId;
    private String region;
    private String restApiId;
    private String stage;

    public String getAwsAccountId()
    {
        return awsAccountId;
    }

    public String getRegion()
    {
        return region;
    }

    public String getRestApiId()
    {
        return restApiId;
    }

    public String getStage()
    {
        return stage;
    }

    /**
     * Parses the parameters for building the Identity self URI and the AWS Policy
     * @param methodArn Usually looks like arn:aws:execute-api:us-west-2:303027209135:boa9doyxae/test/POST/idm
     * @return AuthorizerRequest parameters for the AuthPolicy
     */
    public static AuthorizerRequest fromArn(String methodArn)
    {
        //parse the Arn parts for the AuthPolicy
        if(StringUtils.isEmpty(methodArn)) throw new RuntimeException("methodArn is null");
        String[] methodArnParts = methodArn.split(":");
        if(methodArnParts.length < 6)  throw new RuntimeException(String.format("methodArn is an invalid format %s", methodArn));
        String[] apiGatewayArnTmp = methodArnParts[5].split("/");
        String awsAccountId = methodArnParts[4];
        String region = methodArnParts[3];
        String restApiId = apiGatewayArnTmp[0];
        String stage = apiGatewayArnTmp[1];

        AuthorizerRequest request = new AuthorizerRequest();
        request.awsAccountId = awsAccountId;
        request.region = region;
        request.restApiId = restApiId;
        request.stage = stage;
        return request;
    }
}
