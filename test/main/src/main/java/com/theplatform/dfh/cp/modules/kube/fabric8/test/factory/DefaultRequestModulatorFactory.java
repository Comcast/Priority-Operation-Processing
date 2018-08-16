package com.theplatform.dfh.cp.modules.kube.fabric8.test.factory;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulator;

import static com.theplatform.dfh.cp.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulation.LOW;

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
}