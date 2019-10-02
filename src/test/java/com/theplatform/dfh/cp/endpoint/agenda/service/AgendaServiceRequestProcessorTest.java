package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.resourcepool.service.GetAgendaServiceRequestProcessor;
import com.theplatform.dfh.cp.scheduling.api.AgendaInfo;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.*;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.auth.AuthorizationResponse;
import com.theplatform.dfh.endpoint.api.auth.DataVisibility;
import com.theplatform.dfh.modules.queue.api.ItemQueue;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.api.QueueResult;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
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
    private GetAgendaServiceRequestProcessor processor;

    private ServiceRequest<GetAgendaRequest> getAgendaRequest;

    private ObjectPersister<Insight> mockInsightPersister;
    private ObjectPersister<Agenda> mockAgendaPersister;
    private ObjectPersister<ResourcePool> mockResourcePoolPersister;
    private ItemQueueFactory<AgendaInfo> mockAgendaInfoItemQueueFactory;
    private ItemQueue<AgendaInfo> mockAgendaInfoItemQueue;

    @BeforeMethod
    public void setup()
    {
        getAgendaRequest = new DefaultServiceRequest<>(new GetAgendaRequest("InsightId", 1));
        AuthorizationResponse authorizedResponse = new AuthorizationResponse(null, null, null, DataVisibility.global);
        getAgendaRequest.setAuthorizationResponse(authorizedResponse);
        mockInsightPersister = (ObjectPersister<Insight>)mock(ObjectPersister.class);
        mockAgendaPersister = (ObjectPersister<Agenda>)mock(ObjectPersister.class);
        mockResourcePoolPersister = (ObjectPersister<ResourcePool>)mock(ObjectPersister.class);
        mockAgendaInfoItemQueueFactory = (ItemQueueFactory<AgendaInfo>)mock(ItemQueueFactory.class);
        mockAgendaInfoItemQueue = (ItemQueue<AgendaInfo>)mock(ItemQueue.class);
        doReturn(mockAgendaInfoItemQueue).when(mockAgendaInfoItemQueueFactory).createItemQueue(anyString());

        processor = new GetAgendaServiceRequestProcessor(mockAgendaInfoItemQueueFactory, mockInsightPersister, mockAgendaPersister, mockResourcePoolPersister);
    }

    @Test
    public void testInsightException() throws PersistenceException
    {
        doThrow(new PersistenceException("")).when(mockInsightPersister).retrieve(anyString());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.processPOST(getAgendaRequest);
        Assert.assertNull(getAgendaResponse.getAll());
        Assert.assertNotNull(getAgendaResponse.getErrorResponse());
        Assert.assertEquals(getAgendaResponse.getErrorResponse().getTitle(), PersistenceException.class.getSimpleName());
        verify(mockAgendaInfoItemQueueFactory, times(0)).createItemQueue(anyString());
    }

    @Test
    public void testInsightNotFound() throws PersistenceException
    {
        doReturn(null).when(mockInsightPersister).retrieve(anyString());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.processPOST(getAgendaRequest);
        Assert.assertNull(getAgendaResponse.getAll());
        Assert.assertNotNull(getAgendaResponse.getErrorResponse());
        Assert.assertEquals(getAgendaResponse.getErrorResponse().getTitle(), ObjectNotFoundException.class.getSimpleName());
        verify(mockAgendaInfoItemQueueFactory, times(0)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaInfoPollError() throws PersistenceException
    {
        doReturn(new Insight()).when(mockInsightPersister).retrieve(anyString());
        doReturn(new ResourcePool()).when(mockResourcePoolPersister).retrieve(anyString());
        doReturn(createQueueResult(false, null, "bad times")).when(mockAgendaInfoItemQueue).poll(anyInt());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.processPOST(getAgendaRequest);
        Assert.assertNull(getAgendaResponse.getAll());
        Assert.assertNotNull(getAgendaResponse.getErrorResponse());
        Assert.assertEquals(getAgendaResponse.getErrorResponse().getTitle(), RuntimeServiceException.class.getSimpleName());
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaInfoPollEmpty() throws PersistenceException
    {
        doReturn(new Insight()).when(mockInsightPersister).retrieve(anyString());
        doReturn(new ResourcePool()).when(mockResourcePoolPersister).retrieve(anyString());
        doReturn(createQueueResult(true, null, null)).when(mockAgendaInfoItemQueue).poll(anyInt());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.processPOST(getAgendaRequest);
        Assert.assertEquals(0, getAgendaResponse.getAll().size());
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaInfoPollItem() throws PersistenceException
    {
        doReturn(new Insight()).when(mockInsightPersister).retrieve(anyString());
        doReturn(new ResourcePool()).when(mockResourcePoolPersister).retrieve(anyString());
        doReturn(createQueueResult(
            true,
            Collections.singletonList(new ReadyAgenda()),
            null))
            .when(mockAgendaInfoItemQueue).poll(anyInt());
        doReturn(new Agenda()).when(mockAgendaPersister).retrieve(anyString());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.processPOST(getAgendaRequest);
        Assert.assertEquals(1, getAgendaResponse.getAll().size());
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaInfoPollItems() throws PersistenceException
    {
        doReturn(new Insight()).when(mockInsightPersister).retrieve(anyString());
        doReturn(new ResourcePool()).when(mockResourcePoolPersister).retrieve(anyString());
        doReturn(createQueueResult(
            true,
            Arrays.asList(new ReadyAgenda(), new ReadyAgenda()),
            null))
            .when(mockAgendaInfoItemQueue).poll(anyInt());
        doReturn(new Agenda()).when(mockAgendaPersister).retrieve(anyString());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.processPOST(getAgendaRequest);
        Assert.assertEquals(2, getAgendaResponse.getAll().size());
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaDoesNotExist() throws PersistenceException
    {
        doReturn(new Insight()).when(mockInsightPersister).retrieve(anyString());
        doReturn(new ResourcePool()).when(mockResourcePoolPersister).retrieve(anyString());
        doReturn(createQueueResult(
            true,
            Arrays.asList(new ReadyAgenda(), new ReadyAgenda()),
            null))
            .when(mockAgendaInfoItemQueue).poll(anyInt());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.processPOST(getAgendaRequest);
        Assert.assertNotNull(getAgendaResponse.getAll());
        Assert.assertEquals(0, getAgendaResponse.getAll().size());
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    @DataProvider
    public Object[][] validationExceptionProvider()
    {
        return new Object[][]
            {
                {null, 1, "InsightId is required to getAgenda."},
                {"foo", null, "Count is required to getAgenda."},
                {"foo", 0, "Count must be greater than 0 for getAgenda."},
                {"foo", -1, "Count must be greater than 0 for getAgenda."}
            };
    }

    @Test(dataProvider = "validationExceptionProvider")
    public void testValidationException(String insightId, Integer count, String expectedMessage)
    {
        GetAgendaRequest request = new GetAgendaRequest(insightId, count);

        try
        {
            processor.processPOST(new DefaultServiceRequest<>(request));
            Assert.fail();
        }
        catch(ValidationException e)
        {
            Assert.assertEquals(e.getMessage(), expectedMessage);
        }
    }

    private QueueResult<ReadyAgenda> createQueueResult(boolean successful, Collection<ReadyAgenda> data ,String message)
    {
        return new QueueResult<>(successful, data, message);
    }
}
