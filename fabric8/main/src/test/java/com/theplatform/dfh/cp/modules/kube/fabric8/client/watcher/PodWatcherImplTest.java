package com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher;

import com.theplatform.dfh.cp.modules.kube.client.LogLineAccumulator;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.facade.PodResourceFacade;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.LogLineAccumulatorImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.LogLineAccumulatorTest;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.junit.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class PodWatcherImplTest
{
    @DataProvider(name = "cases")
    Object[][] getCases()
    {
        return new Object[][] {
            { null, PodPhase.SUCCEEDED, null },
            {
                LogLineAccumulatorTest.COMPLETION_TERMINATION, PodPhase.SUCCEEDED,
                LogLineAccumulatorTest.COMPLETION_TERMINATION },
            {
                LogLineAccumulatorTest.COMPLETION_TERMINATION, PodPhase.FAILED,
                LogLineAccumulatorTest.COMPLETION_TERMINATION },
            {
                "NOTTHELINE" + LogLineAccumulatorTest.COMPLETION_TERMINATION, PodPhase.SUCCEEDED,
                LogLineAccumulatorTest.COMPLETION_TERMINATION },
            {
                "NOTTHELINE" + LogLineAccumulatorTest.COMPLETION_TERMINATION, PodPhase.FAILED,
                LogLineAccumulatorTest.COMPLETION_TERMINATION }
        };
    }

    @Test(dataProvider = "cases")
    public void testCompletionWithoutEndOfLogRequirement(String completion, PodPhase lastPhase,
        String completionStringEmitted) throws Exception
    {
        PodWatcherImpl impl = new PodWatcherImpl();
        LogLineAccumulator accumulator = new LogLineAccumulatorImpl();
        accumulator.setCompletionIdentifier(completion);

        CountDownLatch sched = new CountDownLatch(1);
        CountDownLatch fin = new CountDownLatch(1);
        impl.setScheduledLatch(sched);
        impl.setFinishedLatch(fin);
        impl.setLogLineAccumulator(accumulator);
        PodResourceFacade podClient = mock(PodResourceFacade.class);
        impl.setPodResource(podClient);
        impl.setWatch(mock(Watch.class));

        LogWatch logWatch = mock(LogWatch.class);
        when(podClient.watchLog()).thenReturn(logWatch);
        Pod pod1 = new Pod();
        pod1.setStatus(new PodStatus());
        when(podClient.get()).thenReturn(pod1);

        when(logWatch.getOutput()).thenReturn(new ByteArrayInputStream(new byte[] {}));

        Watcher.Action modified = Watcher.Action.MODIFIED;
        Pod pod = new Pod();
        PodStatus podStatus = new PodStatus();
        podStatus.setPhase(PodPhase.RUNNING.getLabel());
        pod.setStatus(podStatus);

        ////////////////////////////////////////////////////////
        // start events
        impl.eventReceived(modified, pod);
        ////////////////////////////////////////////////////////

        LinkedList<ContainerStatus> containerStatuses = new LinkedList<>();
        ContainerStatus e = new ContainerStatus();
        ContainerState state = new ContainerState();
        ContainerStateTerminated terminated = new ContainerStateTerminated();

        boolean succeeded = isSucceeded(lastPhase);

        terminated.setExitCode(succeeded
                               ? 0
                               : 1);
        state.setTerminated(terminated);
        e.setState(state);
        containerStatuses.add(e);
        podStatus.setContainerStatuses(containerStatuses);
        podStatus.setPhase(lastPhase.getLabel());

        ///////////////////////////////////////////////////////
        // finish the events
        impl.eventReceived(modified, pod);
        ///////////////////////////////////////////////////////

        Assert.assertEquals(0, sched.getCount());

        if (completionStringEmitted == null)
        {
            Assert.assertEquals(0, fin.getCount());
        }
        else
        {
            int expectedLatch = succeeded ? 1 : 0;

            Assert.assertEquals(expectedLatch, fin.getCount());
            accumulator.appendLine(completionStringEmitted);
            accumulator.takeAll();
            if(!succeeded)
            {
                accumulator.setCompletionIdentifier(null);
                Assert.assertEquals(0, fin.getCount());
            }

        }
    }

    private boolean isSucceeded(PodPhase lastPhase)
    {
        return lastPhase == PodPhase.SUCCEEDED;
    }
}
