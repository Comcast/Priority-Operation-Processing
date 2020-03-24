package com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher;

import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStateTerminated;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.PodStatus;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FinalPodPhaseInfoTest
{
    final String REASON = "theReason";
    final Integer EXIT_CODE = 15;

    @DataProvider
    public Object[][] testFromPodStatusProvider()
    {
        return new Object[][]
            {
                // with container status
                {
                    createPodStatus(PodPhase.FAILED, REASON, createStatus(EXIT_CODE)),
                    PodPhase.FAILED, REASON, EXIT_CODE
                },
                // with null container status
                {
                    createPodStatus(PodPhase.FAILED, REASON, null),
                    PodPhase.FAILED, REASON, EXIT_CODE
                },
                // with empty container status
                {
                    createPodStatus(PodPhase.FAILED, REASON, new ArrayList<>()),
                    PodPhase.FAILED, REASON, EXIT_CODE
                },
                // with container status
                {
                    createPodStatus(PodPhase.RUNNING, REASON, createStatus(EXIT_CODE)),
                    PodPhase.FAILED, REASON, EXIT_CODE
                }
            };
    }

    @Test(dataProvider = "testFromPodStatusProvider")
    public void testFromPodStatusProvider(PodStatus podStatus, final PodPhase EXPECTED_PHASE, final String EXPECTED_REASON,  final Integer EXPECTED_EXIT_CODE)
    {
        final String NAME = "name";
        FinalPodPhaseInfo info = FinalPodPhaseInfo.fromPodStatus(NAME, createPodStatus(EXPECTED_PHASE, EXPECTED_REASON, createStatus(EXPECTED_EXIT_CODE)));
        Assert.assertEquals(info.exitCode, EXPECTED_EXIT_CODE);
        Assert.assertEquals(info.name, NAME);
        Assert.assertEquals(info.phase, EXPECTED_PHASE);
        Assert.assertEquals(info.reason, EXPECTED_REASON);
    }

    private PodStatus createPodStatus(PodPhase podPhase, String reason, List<ContainerStatus> containerStatuses)
    {
        PodStatus podStatus = new PodStatus();
        podStatus.setReason(reason);
        podStatus.setPhase(podPhase.getLabel());
        podStatus.setContainerStatuses(containerStatuses);
        return podStatus;
    }

    private List<ContainerStatus> createStatus(int exitCode)
    {
        ContainerStatus containerStatus = new ContainerStatus();
        ContainerState containerState = new ContainerState();
        ContainerStateTerminated containerStateTerminated = new ContainerStateTerminated();
        containerStateTerminated.setExitCode(exitCode);
        containerState.setTerminated(containerStateTerminated);
        containerStatus.setState(containerState);
        return Collections.singletonList(containerStatus);
    }
}
