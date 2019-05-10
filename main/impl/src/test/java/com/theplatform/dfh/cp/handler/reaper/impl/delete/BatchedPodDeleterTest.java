package com.theplatform.dfh.cp.handler.reaper.impl.delete;

import com.theplatform.com.dfh.modules.sync.util.ConsumerResult;
import com.theplatform.dfh.cp.handler.reaper.impl.ReaperTestUtil;
import com.theplatform.dfh.cp.handler.reaper.impl.kubernetes.KubernetesPodFacade;
import io.fabric8.kubernetes.api.model.Pod;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BatchedPodDeleterTest
{
    final Instant END_PROCESSING_TIME = Instant.now().plusSeconds(60);
    private KubernetesPodFacade mockKubernetesPodsFacade;
    private BatchedPodDeleter batchedPodDeleter;

    @BeforeMethod
    public void setup()
    {
        mockKubernetesPodsFacade = mock(KubernetesPodFacade.class);
        batchedPodDeleter = new BatchedPodDeleter(mockKubernetesPodsFacade);
    }

    @DataProvider
    public Object[][] deletePodsProvider()
    {
        return new Object[][]
            {
                // no pods to reap
                { ReaperTestUtil.createPodList(0), 1},
                { ReaperTestUtil.createPodList(1), 10},
                { ReaperTestUtil.createPodList(10), 10},
                { ReaperTestUtil.createPodList(11), 10},
                { ReaperTestUtil.createPodList(31), 7},
            };
    }

    @Test(dataProvider = "deletePodsProvider")
    public void testDeletePods(List<Pod> podsToDelete, final int BATCH_SIZE)
    {
        final int EXPECTED_DELETE_CALLS = (int)Math.ceil((double)podsToDelete.size() / (double)BATCH_SIZE);
        batchedPodDeleter.setPodReapBatchSize(BATCH_SIZE);
        ConsumerResult<Pod> consumerResult = batchedPodDeleter.consume(podsToDelete, END_PROCESSING_TIME);
        Assert.assertFalse(consumerResult.isInterrupted());
        verify(mockKubernetesPodsFacade, times(EXPECTED_DELETE_CALLS)).deletePods(any());
    }

    @Test
    public void testDeletePodsExecuteTimeout()
    {
        batchedPodDeleter.setPodReapBatchSize(10);
        ConsumerResult<Pod> consumerResult = batchedPodDeleter.consume(ReaperTestUtil.createPodList(50), Instant.now().minusSeconds(1));
        Assert.assertFalse(consumerResult.isInterrupted());
        verify(mockKubernetesPodsFacade, times(1)).deletePods(any());
    }

    @Test
    public void testDeletePodsException()
    {
        // verifies that no matter if there are exceptions the next batch is reaped
        final int BATCH_SIZE = 8;
        List<Pod> podsToDelete = ReaperTestUtil.createPodList(99);
        final int EXPECTED_DELETE_CALLS = (int)Math.ceil((double)podsToDelete.size() / (double)BATCH_SIZE);
        doThrow(new RuntimeException()).when(mockKubernetesPodsFacade).deletePods(any());
        batchedPodDeleter.setPodReapBatchSize(BATCH_SIZE);
        ConsumerResult<Pod> consumerResult = batchedPodDeleter.consume(podsToDelete, END_PROCESSING_TIME);
        Assert.assertFalse(consumerResult.isInterrupted());
        verify(mockKubernetesPodsFacade, times(EXPECTED_DELETE_CALLS)).deletePods(any());
    }
}
