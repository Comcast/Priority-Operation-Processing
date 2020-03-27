package com.comcast.fission.auth.aws.response;

import org.json.JSONObject;

import java.util.HashMap;

public class PolicyContext extends HashMap<String, String>
{
    public PolicyContext withKeyValue(String key, String value)
    {
        put(key, value);
        return this;
    }
    public String toJson()
    {
        return new JSONObject(this).toString();
    }
}
