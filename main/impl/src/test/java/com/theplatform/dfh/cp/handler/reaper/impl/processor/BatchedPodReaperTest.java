package com.theplatform.dfh.cp.handler.reaper.impl.processor;

import com.theplatform.dfh.cp.handler.reaper.impl.filter.PodLookupFilter;
import com.theplatform.dfh.cp.handler.reaper.impl.kubernetes.KubernetesPodFacade;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BatchedPodReaperTest
{
    private KubernetesPodFacade mockKubernetesPodsFacade;
    private PodLookupFilter mockPodLookupFilter;
    private BatchedPodReaper batchedPodReaper;

    @BeforeMethod
    public void setup()
    {
        mockKubernetesPodsFacade = mock(KubernetesPodFacade.class);
        mockPodLookupFilter = mock(PodLookupFilter.class);
        batchedPodReaper = new BatchedPodReaper(mockPodLookupFilter, mockKubernetesPodsFacade);
    }

    @Test
    public void testDeletePodsExecuteTimeout()
    {
        doReturn(createPodList(50)).when(mockPodLookupFilter).getNextResults();
        batchedPodReaper.setReapRunMaxMinutes(0);
        batchedPodReaper.setPodReapBatchSize(10);
        batchedPodReaper.execute();
        verify(mockKubernetesPodsFacade, times(1)).deletePods(any());
    }

    @DataProvider
    public Object[][] deletePodsProvider()
    {
        return new Object[][]
            {
                // no pods to reap
                { createPodList(0), 1},
                { createPodList(1), 10},
                { createPodList(10), 10},
                { createPodList(11), 10},
                { createPodList(31), 7},
            };
    }

    @Test(dataProvider = "deletePodsProvider")
    public void testDeletePods(List<Pod> podsToDelete, final int BATCH_SIZE)
    {
        final int EXPECTED_DELETE_CALLS = (int)Math.ceil((double)podsToDelete.size() / (double)BATCH_SIZE);
        batchedPodReaper.setPodReapBatchSize(BATCH_SIZE);
        Assert.assertTrue(batchedPodReaper.deletePods(podsToDelete, mockKubernetesPodsFacade));
        verify(mockKubernetesPodsFacade, times(EXPECTED_DELETE_CALLS)).deletePods(any());
    }

    @Test
    public void testDeletePodsTimeout()
    {
        List<Pod> podsToDelete = createPodList(50);
        batchedPodReaper.setReapRunMaxMinutes(0);
        batchedPodReaper.setPodReapBatchSize(10);
        Assert.assertFalse(batchedPodReaper.deletePods(podsToDelete, mockKubernetesPodsFacade));
        verify(mockKubernetesPodsFacade, times(1)).deletePods(any());
    }

    @Test
    public void testDeletePodsException()
    {
        // verifies that no matter if there are exceptions the next batch is reaped
        final int BATCH_SIZE = 8;
        List<Pod> podsToDelete = createPodList(99);
        final int EXPECTED_DELETE_CALLS = (int)Math.ceil((double)podsToDelete.size() / (double)BATCH_SIZE);
        doThrow(new RuntimeException()).when(mockKubernetesPodsFacade).deletePods(any());
        batchedPodReaper.setPodReapBatchSize(BATCH_SIZE);
        batchedPodReaper.deletePods(podsToDelete, mockKubernetesPodsFacade);
        verify(mockKubernetesPodsFacade, times(EXPECTED_DELETE_CALLS)).deletePods(any());
    }

    @Test
    public void testExecuteNoResults()
    {
        doReturn(new LinkedList<Pod>()).when(mockPodLookupFilter).getNextResults();
        batchedPodReaper.execute();
    }

    @Test
    public void testExecuteNullResults()
    {
        doReturn(null).when(mockPodLookupFilter).getNextResults();
        batchedPodReaper.execute();
    }

    public List<Pod> createPodList(int count)
    {
        List<Pod> pods = new LinkedList<>();
        IntStream.range(0, count).forEach(i ->
        {
            Pod pod = new Pod();
            ObjectMeta objectMeta = new ObjectMeta();
            objectMeta.setName(Integer.toString(i));
            pod.setMetadata(objectMeta);
            pods.add(pod);
        });
        return pods;
    }
}
