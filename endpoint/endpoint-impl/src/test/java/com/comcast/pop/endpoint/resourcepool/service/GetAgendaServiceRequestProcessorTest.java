package com.comcast.pop.endpoint.resourcepool.service;

import com.comcast.pop.endpoint.api.*;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comcast.pop.endpoint.progress.AgendaProgressRequestProcessor;
import com.comcast.pop.endpoint.resourcepool.InsightRequestProcessor;
import com.comcast.pop.scheduling.api.AgendaInfo;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import com.comcast.pop.endpoint.api.resourcepool.GetAgendaRequest;
import com.comcast.pop.endpoint.api.auth.AuthorizationResponse;
import com.comcast.pop.endpoint.api.auth.DataVisibility;
import com.comcast.pop.modules.queue.api.ItemQueue;
import com.comcast.pop.modules.queue.api.ItemQueueFactory;
import com.comcast.pop.modules.queue.api.QueueResult;
import com.comcast.pop.persistence.api.ObjectPersister;
import com.comcast.pop.persistence.api.PersistenceException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GetAgendaServiceRequestProcessorTest
{
    private static final String AGENDA_PROGRESS_ID = UUID.randomUUID().toString();

    private GetAgendaServiceRequestProcessor processor;

    private ServiceRequest<GetAgendaRequest> getAgendaRequest;

    private ObjectPersister<Insight> mockInsightPersister;
    private ObjectPersister<Agenda> mockAgendaPersister;
    private ItemQueueFactory<AgendaInfo> mockAgendaInfoItemQueueFactory;
    private ItemQueue<AgendaInfo> mockAgendaInfoItemQueue;
    private ObjectPersister<AgendaProgress> mockAgendaProgressPersister;
    private ObjectPersister<OperationProgress> mockOperationProgressPersister;
    private AgendaProgressRequestProcessor mockAgendaProgressRequestProcessor;
    private InsightRequestProcessor mockInsightRequestProcessor;

    @BeforeMethod
    public void setup()
    {
        getAgendaRequest = new DefaultServiceRequest<>(new GetAgendaRequest("InsightId", 1));
        AuthorizationResponse authorizedResponse = new AuthorizationResponse(null, null, null, DataVisibility.global);
        getAgendaRequest.setAuthorizationResponse(authorizedResponse);
        mockInsightPersister = (ObjectPersister<Insight>)mock(ObjectPersister.class);
        mockAgendaPersister = (ObjectPersister<Agenda>)mock(ObjectPersister.class);
        mockAgendaInfoItemQueueFactory = (ItemQueueFactory<AgendaInfo>)mock(ItemQueueFactory.class);
        mockAgendaInfoItemQueue = (ItemQueue<AgendaInfo>)mock(ItemQueue.class);
        doReturn(mockAgendaInfoItemQueue).when(mockAgendaInfoItemQueueFactory).createItemQueue(anyString());
        mockAgendaProgressPersister = mock(ObjectPersister.class);
        mockOperationProgressPersister = mock(ObjectPersister.class);
        mockAgendaProgressRequestProcessor = mock(AgendaProgressRequestProcessor.class);
        mockInsightRequestProcessor = mock(InsightRequestProcessor.class);
        doReturn(new DefaultDataObjectResponse<AgendaProgress>()).when(mockAgendaProgressRequestProcessor).handleGET(any());

        processor = new GetAgendaServiceRequestProcessor(mockAgendaInfoItemQueueFactory, mockInsightPersister, mockAgendaPersister, mockAgendaProgressPersister, mockOperationProgressPersister);
        processor.setAgendaProgressRequestProcessor(mockAgendaProgressRequestProcessor);
        processor.setInsightRequestProcessor(mockInsightRequestProcessor);
    }

    @Test
    public void testInsightException()
    {
        DataObjectResponse<Insight> insightResponse = new DefaultDataObjectResponse<>(new ErrorResponse(new BadRequestException(), 500, ""));
        doReturn(insightResponse).when(mockInsightRequestProcessor).handleGET(any());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.handlePOST(getAgendaRequest);
        Assert.assertNull(getAgendaResponse.getAll());
        Assert.assertNotNull(getAgendaResponse.getErrorResponse());
        Assert.assertEquals(getAgendaResponse.getErrorResponse().getTitle(), BadRequestException.class.getSimpleName());
        verify(mockAgendaInfoItemQueueFactory, times(0)).createItemQueue(anyString());
    }

    @Test
    public void testInsightNotFound()
    {
        setupInsightLookupMock(false);
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.handlePOST(getAgendaRequest);
        Assert.assertNull(getAgendaResponse.getAll());
        Assert.assertNotNull(getAgendaResponse.getErrorResponse());
        Assert.assertEquals(getAgendaResponse.getErrorResponse().getTitle(), BadRequestException.class.getSimpleName());
        verify(mockAgendaInfoItemQueueFactory, times(0)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaInfoPollError()
    {
        setupInsightLookupMock(true);
        doReturn(createQueueResult(false, null, "bad times")).when(mockAgendaInfoItemQueue).poll(anyInt());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.handlePOST(getAgendaRequest);
        Assert.assertNull(getAgendaResponse.getAll());
        Assert.assertNotNull(getAgendaResponse.getErrorResponse());
        Assert.assertEquals(getAgendaResponse.getErrorResponse().getTitle(), RuntimeServiceException.class.getSimpleName());
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaInfoPollEmpty()
    {
        setupInsightLookupMock(true);
        doReturn(createQueueResult(true, null, null)).when(mockAgendaInfoItemQueue).poll(anyInt());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.handlePOST(getAgendaRequest);
        Assert.assertEquals(0, getAgendaResponse.getAll().size());
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaInfoPollItem() throws PersistenceException
    {
        setupInsightLookupMock(true);
        doReturn(createQueueResult(
            true,
            Collections.singletonList(new ReadyAgenda()),
            null))
            .when(mockAgendaInfoItemQueue).poll(anyInt());
        doReturn(new Agenda()).when(mockAgendaPersister).retrieve(anyString());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.handlePOST(getAgendaRequest);
        Assert.assertEquals(1, getAgendaResponse.getAll().size());
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaInfoPollItems() throws PersistenceException
    {
        setupInsightLookupMock(true);
        doReturn(createQueueResult(
            true,
            Arrays.asList(new ReadyAgenda(), new ReadyAgenda()),
            null))
            .when(mockAgendaInfoItemQueue).poll(anyInt());
        doReturn(new Agenda()).when(mockAgendaPersister).retrieve(anyString());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.handlePOST(getAgendaRequest);
        Assert.assertEquals(2, getAgendaResponse.getAll().size());
        verify(mockAgendaInfoItemQueueFactory, times(1)).createItemQueue(anyString());
    }

    @Test
    public void testAgendaDoesNotExist()
    {
        setupInsightLookupMock(true);
        doReturn(createQueueResult(
            true,
            Arrays.asList(new ReadyAgenda(), new ReadyAgenda()),
            null))
            .when(mockAgendaInfoItemQueue).poll(anyInt());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.handlePOST(getAgendaRequest);
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
            processor.handlePOST(new DefaultServiceRequest<>(request));
            Assert.fail();
        }
        catch(ValidationException e)
        {
            Assert.assertEquals(e.getMessage(), expectedMessage);
        }
    }

    @DataProvider
    public Object[][] withoutOperationProgressProvider()
    {
        return new Object[][]
            {
                {null},
                {createAgendaProgress(null)},
                {createAgendaProgress(Arrays.asList())},
                {createAgendaProgress(Arrays.asList(ProcessingState.WAITING))},
                {createAgendaProgress(Arrays.asList(ProcessingState.WAITING, ProcessingState.WAITING, ProcessingState.WAITING))},
            };
    }

    @Test(dataProvider = "withoutOperationProgressProvider")
    public void testRetrieveExistingAgendaProgressNoProgress(AgendaProgress agendaProgress) throws PersistenceException
    {
        doReturn(agendaProgress).when(mockAgendaProgressPersister).retrieve(AGENDA_PROGRESS_ID);
        List<AgendaProgress> agendaProgresses = new LinkedList<>();
        processor.retrieveExistingAgendaProgress(createAgenda(AGENDA_PROGRESS_ID), agendaProgresses);
        Assert.assertEquals(agendaProgresses.size(), 0);
    }

    @DataProvider
    public Object[][] withOperationProgressProvider()
    {
        return new Object[][]
            {
                {createAgendaProgress(Arrays.asList(ProcessingState.COMPLETE))},
                {createAgendaProgress(Arrays.asList(ProcessingState.EXECUTING))},
                {createAgendaProgress(Arrays.asList(ProcessingState.WAITING, ProcessingState.COMPLETE, ProcessingState.WAITING))},
            };
    }

    @Test(dataProvider = "withOperationProgressProvider")
    public void testRetrieveExistingAgendaProgress(AgendaProgress agendaProgress) throws PersistenceException
    {
        DataObjectResponse<AgendaProgress> response = new DefaultDataObjectResponse<>();
        response.add(agendaProgress);
        doReturn(response).when(mockAgendaProgressRequestProcessor).handleGET(any());
        processor.setAgendaProgressRequestProcessor(mockAgendaProgressRequestProcessor);
        List<AgendaProgress> agendaProgresses = new ArrayList<>();
        processor.retrieveExistingAgendaProgress(createAgenda(AGENDA_PROGRESS_ID), agendaProgresses);
        Assert.assertEquals(agendaProgresses.size(), 1);
    }

    private void setupInsightLookupMock(boolean createInsight)
    {
        DataObjectResponse<Insight> insightResponse = new DefaultDataObjectResponse<>();
        if(createInsight)
            insightResponse.add(new Insight());
        doReturn(insightResponse).when(mockInsightRequestProcessor).handleGET(any());
    }

    /**
     * Creates an AgendaProgress with operations of the specified states
     * @param processingStates Optional list of processing states to apply to sub-OperationProgress objects
     * @return New AgendaProgress with the specified list of operations
     */
    private AgendaProgress createAgendaProgress(List<ProcessingState> processingStates)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        if(processingStates != null)
        {
            agendaProgress.setOperationProgress(
                processingStates.stream().map(ps ->
                {
                    OperationProgress operationProgress = new OperationProgress();
                    operationProgress.setProcessingState(ps);
                    return operationProgress;
                }).collect(Collectors.toList()).toArray(new OperationProgress[0]));
        }
        return agendaProgress;
    }

    private Agenda createAgenda(String agendaProgressId)
    {
        Agenda agenda = new Agenda();
        agenda.setProgressId(agendaProgressId);
        return agenda;
    }

    private QueueResult<ReadyAgenda> createQueueResult(boolean successful, Collection<ReadyAgenda> data ,String message)
    {
        return new QueueResult<>(successful, data, message);
    }
}
