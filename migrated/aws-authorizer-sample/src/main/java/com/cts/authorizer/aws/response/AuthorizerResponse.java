package com.cts.authorizer.aws.response;

import com.amazonaws.auth.policy.Policy;

public class AuthorizerResponse
{
    private String principalId;
    private Policy policy;
    private PolicyContext policyContext;

    public AuthorizerResponse(){}

    public AuthorizerResponse(String principalId, Policy policy)
    {
        this.principalId = principalId;
        this.policy = policy;
    }
    public AuthorizerResponse(String principalId, Policy policy, PolicyContext context)
    {
        this.principalId = principalId;
        this.policy = policy;
        this.policyContext = context;
    }

    public void setPrincipalId(String principalId)
    {
        this.principalId = principalId;
    }

    public void setPolicy(Policy policy)
    {
        this.policy = policy;
    }

    public PolicyContext getPolicyContext()
    {
        return policyContext;
    }

    public void setPolicyContext(PolicyContext policyContext)
    {
        this.policyContext = policyContext;
    }

    public String getPrincipalId()
    {
        return principalId;
    }

    public Policy getPolicy()
    {
        return policy;
    }


    public String toJson()
    {
        return new JsonAuthorizerResponseWriter().writeAuthorizerResponseToString(this);
    }
}
