package com.comcast.pop.modules.kube.fabric8.client.modulator;

import com.comcast.pop.modules.kube.client.CpuRequestModulator;
import com.comcast.pop.modules.kube.client.config.PodConfig;

/**
 *
 */
public class HiLowCpuRequestModulator implements CpuRequestModulator
{
    private String maximumCpuRequest;
    private String minimumCpuRequest;
    private HiLowCpuRequestModulation modulation = HiLowCpuRequestModulation.HI;

    public void setModulation(
        HiLowCpuRequestModulation modulation)
    {
        if(modulation != null)
        {
            this.modulation = modulation;
        }
    }

    public void setMaximumCpuRequest(String maximumCpuRequest)
    {
        this.maximumCpuRequest = maximumCpuRequest;
    }

    public String getMaximumCpuRequest()
    {
        return maximumCpuRequest;
    }

    public void setMinimumCpuRequest(String minimumCpuRequest)
    {
        this.minimumCpuRequest = minimumCpuRequest;
    }

    @Override
    public String getCpuRequest()
    {
        return getCpuRequest(modulation);
    }

    @Override
    public String getCpuLimit()
    {
        return null;
    }

    public String getCpuRequest(HiLowCpuRequestModulation which)
    {
        String ret = null;
        switch (which)
        {
            case HI:
                ret = maximumCpuRequest == null ? minimumCpuRequest : maximumCpuRequest;
                break;
            case LOW:
                ret = minimumCpuRequest == null ? maximumCpuRequest : minimumCpuRequest;
                break;
        }


        return ret;
    }

    public static CpuRequestModulator getCpuRequestModulator(PodConfig podConfig)
    {
        HiLowCpuRequestModulator modulator = new HiLowCpuRequestModulator();
        modulator.setMaximumCpuRequest(podConfig.getCpuMaxRequestCount());
        modulator.setMinimumCpuRequest(podConfig.getCpuMinRequestCount());
        return modulator;
    }
}