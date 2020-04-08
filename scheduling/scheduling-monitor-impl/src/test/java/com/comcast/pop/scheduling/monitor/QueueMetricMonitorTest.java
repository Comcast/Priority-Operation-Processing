package com.comcast.pop.scheduling.monitor;

import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.modules.monitor.metric.LoggingMetricReporterFactory;
import com.comcast.pop.modules.monitor.metric.MetricReporter;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import com.comcast.pop.endpoint.client.ObjectClient;
import com.comcast.pop.modules.queue.api.ItemQueue;
import com.comcast.pop.modules.queue.api.ItemQueueFactory;
import com.comcast.pop.modules.queue.api.QueueResult;
import com.comcast.pop.persistence.api.ObjectPersister;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class QueueMetricMonitorTest
{
    private final String RESOURCE_POOL_ID = "theId";
    private QueueMetricMonitor queueMonitor;

    private ItemQueueFactory<ReadyAgenda> mockReadyAgendaQueueFactory = mock(ItemQueueFactory.class);
    private ItemQueue<ReadyAgenda> mockReadyAgendaQueue = mock(ItemQueue.class);
    private ObjectClient<Insight> mockInsightClient = mock(ObjectClient.class);
    private ObjectPersister<ReadyAgenda> mockReadyAgendaPersister = mock(ObjectPersister.class);

    @BeforeMethod
    public void setup()
    {
        doReturn(mockReadyAgendaQueue).when(mockReadyAgendaQueueFactory).createItemQueue(anyString());
        MetricReporter metricReporter = new MetricReporter();
        metricReporter.register(new LoggingMetricReporterFactory());
        queueMonitor = new QueueMetricMonitor(
            mockReadyAgendaPersister,
            mockInsightClient,
            metricReporter);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testInsightClientError() throws Throwable
    {
        doThrow(new RuntimeException("a")).when(mockInsightClient).getObjects(anyList());
        queueMonitor.monitor(RESOURCE_POOL_ID);
    }

    @Test
    public void testInsightClientNoResults() throws Throwable
    {
        doReturn(new DefaultDataObjectResponse<>()).when(mockInsightClient).getObjects(anyList());
        queueMonitor.monitor(RESOURCE_POOL_ID);
        verify(mockInsightClient, times(0)).getObject(anyString());
    }

    @Test
    public void testQueueSizeFailure() throws Throwable
    {
        doReturn(getInsightFeed(1)).when(mockInsightClient).getObjects(anyList());
        doReturn(new QueueResult<>().setSuccessful(false)).when(mockReadyAgendaQueue).size();
        queueMonitor.monitor(RESOURCE_POOL_ID);
    }

    @Test
    public void testQueueOkaySize() throws Throwable
    {
        doReturn(getInsightFeed(1)).when(mockInsightClient).getObjects(anyList());
        doReturn(new QueueResult<>().setSuccessful(true).setMessage("0")).when(mockReadyAgendaQueue).size();
        queueMonitor.monitor(RESOURCE_POOL_ID);
    }

    @Test
    public void testQueueUnderSize() throws Throwable
    {
        DataObjectResponse<Insight> insightDataObjectResponse = new DefaultDataObjectResponse<>();
        Insight insight = new Insight();
        insight.setQueueSize(1);
        insight.setId("myID");
        insightDataObjectResponse.add(insight);
        doReturn(insightDataObjectResponse).when(mockInsightClient).getObjects(anyList());
        doReturn(new QueueResult<>().setSuccessful(true).setMessage("0")).when(mockReadyAgendaQueue).size();
        Collection<ReadyAgenda> results = Collections.singletonList(new ReadyAgenda());

        queueMonitor.monitor(RESOURCE_POOL_ID);
        verify(mockReadyAgendaPersister, times(1)).retrieve(anyList());
    }

    @DataProvider
    public Object[][] reportSafeInsightTitleProvider()
    {
        return new Object[][]
            {
                {createInsight("a"), "a"},
                {createInsight("a a"), "a_a"},
                {createInsight("a   a"), "a_a"},
                {createInsight(" a   a "), "_a_a_"},
                {createInsight(null), null}
            };
    }

    @Test(dataProvider = "reportSafeInsightTitleProvider")
    public void testGetReportSafeInsightTitle(Insight insight, final String EXPECTED_RESULT)
    {
        Assert.assertEquals(QueueMetricMonitor.getReportSafeInsightTitle(insight), EXPECTED_RESULT);
    }

    private Insight createInsight(String title)
    {
        Insight insight = new Insight();
        insight.setTitle(title);
        return insight;
    }

    private DataObjectResponse<Insight> getInsightFeed(int count)
    {
        DataObjectResponse<Insight> DataObjectResponse = new DefaultDataObjectResponse<>();
        DataObjectResponse.addAll(
            IntStream.range(0, count).mapToObj(i -> new Insight()).collect(Collectors.toList()));
        return DataObjectResponse;
    }

}
