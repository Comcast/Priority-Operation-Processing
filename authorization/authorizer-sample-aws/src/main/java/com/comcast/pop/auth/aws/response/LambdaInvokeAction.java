package com.comcast.pop.auth.aws.response;

public class LambdaInvokeAction implements com.amazonaws.auth.policy.Action
{
    @Override
    public String getActionName()
    {
        return "execute-api:Invoke";
    }
}
