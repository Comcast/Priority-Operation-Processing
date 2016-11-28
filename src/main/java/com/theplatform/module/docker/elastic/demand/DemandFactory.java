package com.theplatform.module.docker.elastic.demand;

import com.amazonaws.util.json.Jackson;

/**
 *
 */
public class DemandFactory
{
    public static RealTimeProxyConfig getRealTimeProxyConfig(S3DemandClient s3DemandClient)
    {
        String json = s3DemandClient.getDemandJson();
        return Jackson.fromJsonString(json, RealTimeProxyConfig.class);
    }
}
