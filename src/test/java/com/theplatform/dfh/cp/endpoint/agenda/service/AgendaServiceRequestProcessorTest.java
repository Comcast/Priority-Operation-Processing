package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.scheduling.api.AgendaInfo;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.modules.queue.api.ItemQueue;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.api.QueueResult;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AgendaServiceRequestProcessorTest
{
    private AgendaServiceRequestProcessor processor;

    private GetAgendaRequest getAgendaRequest = new GetAgendaRequest("InsightId", 1);

    private ObjectPersister<Insight> mockInsightPersister;
    private ObjectPersister<Agenda> mockAgendaPersister;
    private ItemQueueFactory<AgendaInfo> mockAgendaInfoItemQueueFactory;
    private ItemQueue<AgendaInfo> mockAgendaInfoItemQueue;

    @BeforeMethod
    public void setup()
    {
        mockInsightPersister = (ObjectPersister<Insight>)mock(ObjectPersister.class);
        mockAgendaPersister = (ObjectPersister<Agenda>)mock(ObjectPersister.class);
        mockAgendaInfoItemQueueFactory = (ItemQueueFactory<AgendaInfo>)mock(ItemQueueFactory.class);
        mockAgendaInfoItemQueue = (ItemQueue<AgendaInfo>)mock(ItemQueue.class);
        doReturn(mockAgendaInfoItemQueue).when(mockAgendaInfoItemQueueFactory).createItemQueue(anyString());

        processor = new AgendaServiceRequestProcessor(mockAgendaInfoItemQueueFactory, mockInsightPersister, mockAgendaPersister);
    }

    @Test
    public void testInsightException() throws PersistenceException
    {
        doThrow(new PersistenceException("")).when(mockInsightPersister).retrieve(anyString());
        Assert.assertNull(processor.processRequest(getAgendaRequest));
        verify(mockAgendaInfoItemQueueFactory, times(0)).createItemQueue(anyString());
    }

    @Test
    public void testInsightNotFound() throws PersistenceException
    {
        doReturn(null).when(mockInsightPersister).retrieve(anyString());
        Assert.assertNull(processor.processRequest(getAgendaRequest));
        verify(mockAgendaInfoItemQueueFactory, times(0)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaInfoPollError() throws PersistenceException
    {
        doReturn(new Insight()).when(mockInsightPersister).retrieve(anyString());
        doReturn(createQueueResult(false, null, "bad times")).when(mockAgendaInfoItemQueue).poll(anyInt());
        Assert.assertNull(processor.processRequest(getAgendaRequest));
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaInfoPollEmpty() throws PersistenceException
    {
        doReturn(new Insight()).when(mockInsightPersister).retrieve(anyString());
        doReturn(createQueueResult(true, null, null)).when(mockAgendaInfoItemQueue).poll(anyInt());
        GetAgendaResponse getAgendaResponse = processor.processRequest(getAgendaRequest);
        Assert.assertEquals(0, getAgendaResponse.getAgendas().size());
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaInfoPollItem() throws PersistenceException
    {
        doReturn(new Insight()).when(mockInsightPersister).retrieve(anyString());
        doReturn(createQueueResult(
            true,
            Collections.singletonList(new ReadyAgenda()),
            null))
            .when(mockAgendaInfoItemQueue).poll(anyInt());
        doReturn(new Agenda()).when(mockAgendaPersister).retrieve(anyString());
        GetAgendaResponse getAgendaResponse = processor.processRequest(getAgendaRequest);
        Assert.assertEquals(1, getAgendaResponse.getAgendas().size());
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaInfoPollItems() throws PersistenceException
    {
        doReturn(new Insight()).when(mockInsightPersister).retrieve(anyString());
        doReturn(createQueueResult(
            true,
            Arrays.asList(new ReadyAgenda(), new ReadyAgenda()),
            null))
            .when(mockAgendaInfoItemQueue).poll(anyInt());
        doReturn(new Agenda()).when(mockAgendaPersister).retrieve(anyString());
        GetAgendaResponse getAgendaResponse = processor.processRequest(getAgendaRequest);
        Assert.assertEquals(2, getAgendaResponse.getAgendas().size());
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaDoesNotExist() throws PersistenceException
    {
        doReturn(new Insight()).when(mockInsightPersister).retrieve(anyString());
        doReturn(createQueueResult(
            true,
            Arrays.asList(new ReadyAgenda(), new ReadyAgenda()),
            null))
            .when(mockAgendaInfoItemQueue).poll(anyInt());
        GetAgendaResponse getAgendaResponse = processor.processRequest(getAgendaRequest);
        Assert.assertEquals(0, getAgendaResponse.getAgendas().size());
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    private QueueResult<ReadyAgenda> createQueueResult(boolean successful, Collection<ReadyAgenda> data ,String message)
    {
        return new QueueResult<>(successful, data, message);
    }
}
