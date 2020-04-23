package com.comcast.pop.handler.reaper.impl.filter;

import com.comcast.pop.handler.reaper.impl.kubernetes.KubernetesPodFacade;
import com.comcast.pop.modules.kube.fabric8.client.watcher.PodPhase;
import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStateTerminated;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReapPodLookupFilterTest
{
    private static Logger logger = LoggerFactory.getLogger(ReapPodLookupFilterTest.class);

    private final Instant END_PROCESSING_TIME = Instant.now().plusSeconds(60);

    // no confusion about what now is across the tests
    private final Instant NOW = Instant.now();

    private final int REAP_AGE_MINUTES = 240;
    private final Instant REAP_UPPER_BOUND_UTC = NOW.minusSeconds(60*REAP_AGE_MINUTES);
    private ReapPodLookupFilter reapPodLookupFilter;
    private KubernetesPodFacade mockKubernetesPodFacade;

    @BeforeMethod
    public void setup()
    {
        mockKubernetesPodFacade = mock(KubernetesPodFacade.class);
        reapPodLookupFilter = new ReapPodLookupFilter(mockKubernetesPodFacade, REAP_UPPER_BOUND_UTC);
    }

    @Test
    public void testReset()
    {
        reapPodLookupFilter.withPodPhases(PodPhase.SUCCEEDED);

        reapPodLookupFilter.produce(END_PROCESSING_TIME);
        verify(mockKubernetesPodFacade, times(1)).lookupPods(any(), any());

        // no additional calls made
        reapPodLookupFilter.produce(END_PROCESSING_TIME);
        verify(mockKubernetesPodFacade, times(1)).lookupPods(any(), any());

        // reset and mockKubernetesPodFacade called again
        reapPodLookupFilter.reset();
        reapPodLookupFilter.produce(END_PROCESSING_TIME);
        verify(mockKubernetesPodFacade, times(2)).lookupPods(any(), any());
    }

    @DataProvider
    public Object[][] testAppendPodsByPhaseStatusProvider()
    {
        return new Object[][]
            {
                // no pods to reap
                {
                    new ArrayList<>(), new ArrayList<>()
                },
                // no pods to reap
                {
                    Arrays.asList(
                        createPod("noreap1", PodPhase.FAILED, createStatus(NOW)),
                        createPod("noreap2", PodPhase.FAILED, createStatus(NOW))
                    ),
                    new ArrayList<>()
                },
                // mix of pods to reap
                {
                    Arrays.asList(
                        createPod("reap1", PodPhase.FAILED, createStatus(REAP_UPPER_BOUND_UTC)), // exactly the same reap time
                        createPod("reap2", PodPhase.FAILED, createStatus(REAP_UPPER_BOUND_UTC.minusSeconds(1))),
                        createPod("noreap2", PodPhase.FAILED, createStatus(NOW.minusSeconds(60 * (REAP_AGE_MINUTES - 10))))
                    ),
                    Arrays.asList(
                        "reap1",
                        "reap2"
                    )
                },
                // only pods to reap
                {
                    Arrays.asList(
                        createPod("reap1", PodPhase.FAILED, createStatus(REAP_UPPER_BOUND_UTC)),
                        createPod("reap2", PodPhase.FAILED, createStatus(REAP_UPPER_BOUND_UTC))
                    ),
                    Arrays.asList(
                        "reap1",
                        "reap2"
                    )
                }
            };
    }

    @Test(dataProvider = "testAppendPodsByPhaseStatusProvider")
    public void testAppendPodsByPhaseStatus(List<Pod> allFailedPods, List<String> expectedReapPodNames)
    {
        List<Pod> podResults = new ArrayList<>();
        doReturn(allFailedPods).when(mockKubernetesPodFacade).lookupPods(any(), any());

        reapPodLookupFilter.appendPodsByPhaseStatusAndAge(PodPhase.FAILED, podResults);
        // all the expected names should be in the results
        Assert.assertTrue(expectedReapPodNames.stream().allMatch(
            podName ->
            {
                boolean found = podResults.stream().anyMatch(pod -> pod.getMetadata().getName().equals(podName));
                if(!found)
                    logger.error("Failed to find expected pod from filter: {}", podName);
                return found;
            }
        ));
        Assert.assertEquals(expectedReapPodNames.size(), podResults.size());
    }

    @DataProvider
    public Object[][] testIsPodPastAgeAbnormalExitProvider()
    {
        return new Object[][]
            {
                // future
                {
                    NOW.plusSeconds(60 * 10),
                    false
                },
                // now
                {
                    NOW,
                    false
                },
                // before reap age
                {
                    NOW.minusSeconds(60 * (REAP_AGE_MINUTES - 1)),
                    false
                },
                // beyond reap age
                {
                    NOW.minusSeconds(60 * 250),
                    true
                }
            };
    }

    @Test(dataProvider = "testIsPodPastAgeAbnormalExitProvider")
    public void testIsPodPastAgeAbnormalExit(Instant startTime, final boolean EXPECT_REAP)
    {
        Assert.assertEquals(ReapPodLookupFilter.isPodPastAge(
            createPod(
                "Test",
                PodPhase.FAILED,
                startTime,
                null // This is the abnormal exit (kubernetes basically never started any containers)
            ),
            REAP_UPPER_BOUND_UTC
        ), EXPECT_REAP);
    }

    @Test
    public void testInvalidTimeFormatString()
    {
        Assert.assertFalse(
            reapPodLookupFilter.isPodPastAge(
                createPod("thePod", PodPhase.FAILED, createStatus("invalid time string")),
                REAP_UPPER_BOUND_UTC));
    }

    /**
     * Create pods that launched exited normally
     */
    private Pod createPod(String podName, PodPhase podPhase, List<ContainerStatus> containerStatuses)
    {
        Assert.assertNotNull(containerStatuses);
        Assert.assertTrue(containerStatuses.size() > 0, "This method requires 1+ container statuses");
        return createPod(podName, podPhase, null, containerStatuses);
    }

    private Pod createPod(String podName, PodPhase podPhase, Instant statusStartTime, List<ContainerStatus> containerStatuses)
    {
        Pod pod = new Pod();
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(podName);
        pod.setMetadata(objectMeta);
        PodStatus podStatus = new PodStatus();
        podStatus.setPhase(podPhase.getLabel());
        if(statusStartTime != null)
            podStatus.setStartTime(statusStartTime.toString());
        podStatus.setContainerStatuses(containerStatuses);
        pod.setStatus(podStatus);
        return pod;
    }

    private List<ContainerStatus> createStatus(Instant statusEndTime)
    {
        return createStatus(statusEndTime.toString());
    }

    private List<ContainerStatus> createStatus(String finishedAt)
    {
        ContainerStatus containerStatus = new ContainerStatus();
        ContainerState containerState = new ContainerState();
        ContainerStateTerminated containerStateTerminated = new ContainerStateTerminated();
        containerStateTerminated.setFinishedAt(finishedAt);
        containerStateTerminated.setExitCode(0);
        containerState.setTerminated(containerStateTerminated);
        containerStatus.setState(containerState);
        return Collections.singletonList(containerStatus);
    }
}
