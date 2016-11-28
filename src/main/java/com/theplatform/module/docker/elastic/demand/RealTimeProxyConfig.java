package com.theplatform.module.docker.elastic.demand;

/**
 *
 */
public class RealTimeProxyConfig
{
    private String demand;

    public RealTimeProxyConfig()
    {
    }

    public RealTimeProxyConfig(String demand)
    {
        this.demand = demand;
    }

    public void setDemand(String demand)
    {
        this.demand = demand;
    }

    public boolean isMoreDesired()
    {
        return DemandType.more.name().equals(demand);
    }

}
