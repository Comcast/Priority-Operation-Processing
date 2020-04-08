package com.comcast.pop.modules.kube.fabric8.test;

import com.comcast.pop.modules.kube.client.CpuRequestModulator;

public class SimpleCpuRequestModulator implements CpuRequestModulator
{
    private String cpuRequest;
    private String cpuLimit;

    public SimpleCpuRequestModulator(String cpuRequest, String cpuLimit)
    {
        this.cpuRequest = cpuRequest;
        this.cpuLimit = cpuLimit;
    }

    @Override
    public String getCpuRequest()
    {
        return cpuRequest;
    }

    public SimpleCpuRequestModulator setCpuRequest(String cpuRequest)
    {
        this.cpuRequest = cpuRequest;
        return this;
    }

    public SimpleCpuRequestModulator setCpuLimit(String cpuLimit)
    {
        this.cpuLimit = cpuLimit;
        return this;
    }

    @Override
    public String getCpuLimit()
    {
        return cpuLimit;
    }
}
