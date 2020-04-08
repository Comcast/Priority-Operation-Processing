package com.comcast.pop.modules.kube.client;

/**
 *
 */
public interface CpuRequestModulator
{
    public String getCpuRequest();
    public String getCpuLimit();
}
