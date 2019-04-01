package com.theplatform.dfh.cp.modules.kube.fabric8.client.modulator;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulation;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.theplatform.dfh.cp.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulation.HI;
import static com.theplatform.dfh.cp.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulation.LOW;

/**
 *
 */
public class HiLowCpuRequestModulatorTest
{
    @DataProvider(name = "params")
    public static Object[][] params()
    {
        return new Object[][] {
            //flipDirection    low       ,  high       , expected
            { null, (String) null, (String) null, (String) null },
            { HI, (String) null, (String) null, (String) null },
            { null, "1", (String) null, "1" },
            { HI, "1", (String) null, "1" },
            {HI, null, null, null},
            { LOW, (String) null, "1", "1" },
            { LOW, "1", (String) null, "1" },
            { LOW, "1", "2", "1" },
            { HI, "1", "2", "2" },
        };
    }

    @Test(dataProvider = "params")
    public void testDefaultAllNull(HiLowCpuRequestModulation direction, String low, String high,
        String expected)
        throws Exception
    {
        HiLowCpuRequestModulator cpuRequestModulator = new HiLowCpuRequestModulator();
        cpuRequestModulator.setMaximumCpuRequest(high);
        cpuRequestModulator.setMinimumCpuRequest(low);
        HiLowCpuRequestModulation which = direction;
        cpuRequestModulator.setModulation(which);
        Assert.assertEquals(cpuRequestModulator.getCpuRequest(), expected);
    }
}
