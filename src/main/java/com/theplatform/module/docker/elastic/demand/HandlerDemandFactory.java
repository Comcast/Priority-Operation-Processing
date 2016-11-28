package com.theplatform.module.docker.elastic.demand;

/**
 *
 */
public class HandlerDemandFactory
{
    private boolean demandPresent;

    private S3DemandClient s3DemandClient;

    public void setS3DemandClient(S3DemandClient s3DemandClient)
    {
        this.s3DemandClient = s3DemandClient;
    }

    public boolean isDemandPresent()
    {
        if(s3DemandClient == null)
        {
            return demandPresent;
        }
        else
        {
            RealTimeProxyConfig realTimeProxyConfig = DemandFactory.getRealTimeProxyConfig(s3DemandClient);
            return realTimeProxyConfig.isMoreDesired();
        }
    }
}
