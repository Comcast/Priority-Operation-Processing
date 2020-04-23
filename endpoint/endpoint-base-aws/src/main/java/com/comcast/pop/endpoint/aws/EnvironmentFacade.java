package com.comcast.pop.endpoint.aws;

import java.util.Map;

/**
 * Wrapper class for environment variable access (primarily helpful for unit testing)
 */
public class EnvironmentFacade
{
    public String getEnv(String var)
    {
        return System.getenv(var);
    }

    public Map<String, String> getEnv()
    {
        return System.getenv();
    }
}
