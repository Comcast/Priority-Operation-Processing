package com.theplatform.dfh.cp.modules.kube.fabric8.test;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodPhase;
import io.fabric8.kubernetes.api.model.PodStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PodPhaseTest
{

    @Test
    void testFromPodStatus()
    {
        PodPhase podPhase = PodPhase.SUCCEEDED;
        PodStatus podStatus = new PodStatus();
        podStatus.setPhase("succeeded");
        PodPhase result = PodPhase.fromPodStatus(podStatus);
        Assert.assertEquals(result, podPhase);
    }
}
