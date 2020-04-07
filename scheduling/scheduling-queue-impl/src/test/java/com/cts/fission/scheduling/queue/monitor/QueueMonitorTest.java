package com.cts.fission.scheduling.queue.monitor;

import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.cts.fission.scheduling.queue.InsightScheduleInfo;
import com.cts.fission.scheduling.queue.algorithm.AgendaScheduler;
import com.cts.fission.scheduling.queue.algorithm.AgendaSchedulerFactory;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.modules.queue.api.ItemQueue;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.api.QueueResult;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class QueueMonitorTest
{
    private final String RESOURCE_POOL_ID = "theId";
    private QueueMonitor queueMonitor;

    private ItemQueueFactory<ReadyAgenda> mockReadyAgendaQueueFactory = mock(ItemQueueFactory.class);
    private ItemQueue<ReadyAgenda> mockReadyAgendaQueue = mock(ItemQueue.class);
    private ObjectClient<Insight> mockInsightClient = mock(ObjectClient.class);
    private ObjectPersister<InsightScheduleInfo> mockInsightSchedulingInfoPersister = mock(ObjectPersister.class);
    private ObjectPersister<ReadyAgenda> mockReadyAgendaPersister = mock(ObjectPersister.class);
    private ObjectClient<Customer> mockCustomerClient = mock(ObjectClient.class);

    private AgendaSchedulerFactory mockAgendaSchedulerFactory = mock(AgendaSchedulerFactory.class);
    private AgendaScheduler mockAgendaScheduler = mock(AgendaScheduler.class);

    @BeforeMethod
    public void setup()
    {
        doReturn(mockReadyAgendaQueue).when(mockReadyAgendaQueueFactory).createItemQueue(anyString());
        queueMonitor = new QueueMonitor(
            mockReadyAgendaQueueFactory,
            mockReadyAgendaPersister,
            mockInsightClient,
            mockCustomerClient,
            mockInsightSchedulingInfoPersister);
        queueMonitor.setAgendaSchedulerFactory(mockAgendaSchedulerFactory);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testInsightClientError() throws Throwable
    {
        doThrow(new RuntimeException("a")).when(mockInsightClient).getObjects(anyList());
        queueMonitor.processResourcePool(RESOURCE_POOL_ID);
    }

    @Test
    public void testInsightClientNoResults() throws Throwable
    {
        doReturn(new DefaultDataObjectResponse<>()).when(mockInsightClient).getObjects(anyList());
        queueMonitor.processResourcePool(RESOURCE_POOL_ID);
        verify(mockInsightSchedulingInfoPersister, times(0)).retrieve(anyString());
    }

    @Test
    public void testQueueSizeFailure() throws Throwable
    {
        doReturn(getInsightFeed(1)).when(mockInsightClient).getObjects(anyList());
        doReturn(new QueueResult<>().setSuccessful(false)).when(mockReadyAgendaQueue).size();
        queueMonitor.processResourcePool(RESOURCE_POOL_ID);
        verify(mockAgendaSchedulerFactory, times(0)).getAgendaScheduler(any(), any(), any());
    }

    @Test
    public void testQueueOkaySize() throws Throwable
    {
        doReturn(getInsightFeed(1)).when(mockInsightClient).getObjects(anyList());
        doReturn(new QueueResult<>().setSuccessful(true).setMessage("0")).when(mockReadyAgendaQueue).size();
        queueMonitor.processResourcePool(RESOURCE_POOL_ID);
        verify(mockAgendaSchedulerFactory, times(0)).getAgendaScheduler(any(), any(), any());
    }

    @Test
    public void testQueueUnderSize() throws Throwable
    {
        doReturn(mockAgendaScheduler).when(mockAgendaSchedulerFactory).getAgendaScheduler(any(), any(), any());
        DataObjectResponse<Insight> insightDataObjectResponse = new DefaultDataObjectResponse<>();
        Insight insight = new Insight();
        insight.setQueueSize(1);
        insightDataObjectResponse.add(insight);
        doReturn(insightDataObjectResponse).when(mockInsightClient).getObjects(anyList());
        doReturn(new QueueResult<>().setSuccessful(true).setMessage("0")).when(mockReadyAgendaQueue).size();
        Collection<ReadyAgenda> results = Collections.singletonList(new ReadyAgenda());
        doReturn(results).when(mockAgendaScheduler).schedule(anyInt(), any(Insight.class), any(InsightScheduleInfo.class));

        queueMonitor.processResourcePool(RESOURCE_POOL_ID);
        verify(mockAgendaSchedulerFactory, times(1)).getAgendaScheduler(any(), any(), any());
        verify(mockReadyAgendaPersister, times(1)).delete(anyString());
        verify(mockReadyAgendaQueue, times(1)).add(any(ReadyAgenda.class));
    }

    private DataObjectResponse<Insight> getInsightFeed(int count)
    {
        DataObjectResponse<Insight> DataObjectResponse = new DefaultDataObjectResponse<>();
        DataObjectResponse.addAll(
            IntStream.range(0, count).mapToObj(i -> new Insight()).collect(Collectors.toList()));
        return DataObjectResponse;
    }

}
