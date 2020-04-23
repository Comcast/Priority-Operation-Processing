package com.comcast.pop.modules.kube.fabric8.test.factory;

import com.comcast.pop.modules.kube.fabric8.test.SimpleCpuRequestModulator;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import com.comcast.pop.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulator;

import static com.comcast.pop.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulation.LOW;

public class DefaultRequestModulatorFactory
{
    public static HiLowCpuRequestModulator getHiLowCpuRequestModulator()
    {
        HiLowCpuRequestModulator hiLowCpuRequestModulator = new HiLowCpuRequestModulator();
        hiLowCpuRequestModulator.setMinimumCpuRequest("4000m");
        hiLowCpuRequestModulator.setMaximumCpuRequest("8000m");
        hiLowCpuRequestModulator.setModulation(LOW);
        return hiLowCpuRequestModulator;
    }

    public static SimpleCpuRequestModulator getSimpleCpuRequestModulator(PodConfig podConfig)
    {
        return new SimpleCpuRequestModulator(podConfig.getCpuMinRequestCount(), podConfig.getCpuMaxRequestCount());
    }
}