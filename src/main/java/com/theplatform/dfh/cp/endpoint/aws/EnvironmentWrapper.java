package com.theplatform.dfh.cp.endpoint.aws;

import java.util.Map;

/**
 * Wrapper class for environment variable access (primarily helpful for unit testing)
 */
public class EnvironmentWrapper
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
