package com.comcast.pop.auth.aws.response;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Statement;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizerOutputBuilder
{
    private static final Logger logger = LoggerFactory.getLogger(AuthorizerOutputBuilder.class);

    public static final String PRINCIPAL_ID_PREFIX = "user|";

    private String methodArn = null;
    private Statement.Effect statementEffect = Statement.Effect.Deny;

    public AuthorizerOutputBuilder withMethodArn(String methodArn)
    {
        this.methodArn = methodArn;
        return this;
    }

    public AuthorizerOutputBuilder withStatementEffect(Statement.Effect statementEffect)
    {
        this.statementEffect = statementEffect;
        return this;
    }

    /**
     * Builds the string the authorizer will respond with
     * @return The json string representation of the authorizer response
     */
    public String build()
    {
        validateBuilder();

        // if the token is valid, a policy must be generated which will allow or deny access to the client
        // if access is denied, the client will receive a 403 Access Denied response
        // if access is allowed, API Gateway will proceed with the backend integration configured on the method that was called
        // this function must generate a policy that is associated with the recognized principal user identifier.
        // depending on your use case, you might store policies in a DB, or generate them on the fly

        // AUTH CACHING IS VERY BAD AT THE TIME OF THIS IMPLEMENTATION
        // The policy is cached for 5 minutes by default (TTL is configurable in the authorizer).
        // Once a token is accepted for one resource+verb all subsequent calls to ANY resource on the API Gateway
        // with the same token will have access to call the lambda until the TTL expires (if configured).

        //build an aws policy context with our auth response so the resulting lambda can access it.
        DefaultPolicyContext policyContext = new DefaultPolicyContext();

        String userId = policyContext.retrieveUserId();
        String principalId = PRINCIPAL_ID_PREFIX + (userId == null ? "" : userId);

        PolicyBuilder policyBuilder = new PolicyBuilder(AuthorizerRequest.fromArn(methodArn));
        // TODO / WISHLIST: Configure the statement(s) to match that of the actual permissions of the
        // token so the caching is accurate (not trivial!). This currently indicates to the
        // API Gateway token cache: your token is allowed/denied for any resource and/or verb.
        policyBuilder.withStatement(new LambdaInvokeAction(), statementEffect, "*", "*");
        Policy policy = policyBuilder.build(principalId);

        //for reference on the output format
        // https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-lambda-authorizer-output.html
        // https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-use-lambda-authorizer.html
        // generate the response string
        return new AuthorizerResponse(principalId, policy, policyContext).toJson();
    }

    private void validateBuilder()
    {
        if(StringUtils.isBlank(methodArn))
            throw new IllegalArgumentException("methodArn must be specified to build the authorizer output string");
    }
}
