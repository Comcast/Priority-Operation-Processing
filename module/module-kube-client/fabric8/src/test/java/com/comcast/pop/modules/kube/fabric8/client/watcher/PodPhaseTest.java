package com.comcast.pop.modules.kube.fabric8.client.watcher;

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
