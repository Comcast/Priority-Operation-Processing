package com.theplatform.dfh.cp.endpoint.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.facility.insight.mapper.InsightSelector;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.auth.MPXAuthorizationResponseBuilder;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AgendaRequestProcessorTest
{
    private AgendaRequestProcessor agendaRequestProcessor;
    private ObjectPersister<Agenda> mockAgendaPersister;
    private ObjectPersister<ReadyAgenda> mockReadyAgendaPersister;
    private ObjectClient<AgendaProgress> mockAgendaProgressClient;
    private ObjectClient<OperationProgress> mockOperationProgressClient;
    private InsightSelector mockInsightSelector;

    @BeforeMethod
    void setUp()
    {
        mockAgendaPersister = mock(ObjectPersister.class);
        mockReadyAgendaPersister = mock(ObjectPersister.class);
        mockAgendaProgressClient = mock(ObjectClient.class);
        mockOperationProgressClient = mock(ObjectClient.class);
        mockInsightSelector = mock(InsightSelector.class);
        agendaRequestProcessor = new AgendaRequestProcessor(mockAgendaPersister, mockReadyAgendaPersister, mockAgendaProgressClient, mockOperationProgressClient, mockInsightSelector);
        Mockito.when(mockInsightSelector.select(Mockito.any())).thenReturn(new Insight());
    }

    @Test
    void testHandlePost() throws PersistenceException
    {
        int numOps = 2;
        Agenda agenda = getAgenda(numOps);

        AgendaProgress agendaProgressResponse = new AgendaProgress();
        agendaProgressResponse.setId(UUID.randomUUID().toString());
        DataObjectResponse<AgendaProgress> dataObjectResponse = new DefaultDataObjectResponse<>();
        dataObjectResponse.add(agendaProgressResponse);
        doReturn(dataObjectResponse).when(mockAgendaProgressClient).persistObject(any());

        doReturn(new Agenda()).when(mockAgendaPersister).persist(any());

        DataObjectResponse<OperationProgress> opProgressResponse = new DefaultDataObjectResponse<>();
        opProgressResponse.add(new OperationProgress());
        doReturn(opProgressResponse).when(mockOperationProgressClient).persistObject(any());

        DefaultDataObjectRequest<Agenda> request = new DefaultDataObjectRequest<>();
        request.setDataObject(agenda);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());
        DataObjectResponse<Agenda> response = agendaRequestProcessor.handlePOST(request);
        Assert.assertFalse(response.isError());

        verify(mockOperationProgressClient, times(numOps)).persistObject(any());
        verify(mockReadyAgendaPersister, times(1)).persist(any());
    }

    @Test
    void testInsightNotFound() throws PersistenceException
    {
        doReturn(null).when(mockInsightSelector).select(any());

        DefaultDataObjectRequest<Agenda> request = new DefaultDataObjectRequest<>();
        request.setDataObject(getAgenda(2));
        request.setCid(UUID.randomUUID().toString());
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());

        DataObjectResponse<Agenda> response = agendaRequestProcessor.handlePOST(request);
        Assert.assertTrue(response.isError());
        ErrorResponse errorResponse = response.getErrorResponse();
        Assert.assertEquals(errorResponse.getCorrelationId(), request.getCID());
        Assert.assertEquals(errorResponse.getTitle(), "ObjectNotFoundException");
        Assert.assertEquals(errorResponse.getResponseCode(), (Integer) 404);
        Assert.assertNull(response.getFirst());

        // verify objects weren't created
        verify(mockOperationProgressClient, times(0)).persistObject(any());
        verify(mockAgendaProgressClient, times(0)).persistObject(any());
        verify(mockAgendaPersister, times(0)).persist(any());
        verify(mockReadyAgendaPersister, times(0)).persist(any());
    }

    @Test
    void testAgendaProgressPersistError() throws PersistenceException
    {
        String cid = UUID.randomUUID().toString();
        DataObjectResponse<AgendaProgress> agendaProgressResponse = new DefaultDataObjectResponse<>();
        agendaProgressResponse.setErrorResponse(ErrorResponseFactory.unauthorized("Unauthorized request.", cid));
        doReturn(agendaProgressResponse).when(mockAgendaProgressClient).persistObject(any());

        DefaultDataObjectRequest<Agenda> request = new DefaultDataObjectRequest<>();
        request.setDataObject(getAgenda(1));
        request.setCid(cid);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());

        DataObjectResponse<Agenda> response = agendaRequestProcessor.handlePOST(request);
        Assert.assertTrue(response.isError());
        ErrorResponse errorResponse = response.getErrorResponse();
        Assert.assertEquals(errorResponse.getCorrelationId(), cid);
        Assert.assertEquals(errorResponse.getResponseCode(), agendaProgressResponse.getErrorResponse().getResponseCode());
        Assert.assertEquals(errorResponse.getTitle(), agendaProgressResponse.getErrorResponse().getTitle());

        // verify objects weren't created
        verify(mockAgendaPersister, times(0)).persist(any());
        verify(mockReadyAgendaPersister, times(0)).persist(any());
    }

    @Test
    void testOperationProgressPersistError() throws PersistenceException
    {
        String cid = UUID.randomUUID().toString();

        Agenda agenda = getAgenda(1);
        DefaultDataObjectRequest<Agenda> request = new DefaultDataObjectRequest<>();
        request.setDataObject(agenda);
        request.setCid(cid);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());

        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setId(UUID.randomUUID().toString());
        DataObjectResponse<AgendaProgress> agendaProgressResponse = new DefaultDataObjectResponse<>();
        agendaProgressResponse.add(agendaProgress);
        doReturn(agendaProgressResponse).when(mockAgendaProgressClient).persistObject(any());

        DataObjectResponse<OperationProgress> opProgressResponse = new DefaultDataObjectResponse<>();
        opProgressResponse.setErrorResponse(ErrorResponseFactory.badRequest("Bad request", cid));
        doReturn(opProgressResponse).when(mockOperationProgressClient).persistObject(any());

        DataObjectResponse<Agenda> response = agendaRequestProcessor.handlePOST(request);
        Assert.assertTrue(response.isError());
        ErrorResponse errorResponse = response.getErrorResponse();
        Assert.assertEquals(errorResponse.getCorrelationId(), cid);
        Assert.assertEquals(errorResponse.getResponseCode(), opProgressResponse.getErrorResponse().getResponseCode());
        Assert.assertEquals(errorResponse.getTitle(), opProgressResponse.getErrorResponse().getTitle());

        // verify agendaProgress was cleaned up
        verify(mockAgendaProgressClient, times(1)).deleteObject(any());

        // verify objects weren't created
        verify(mockAgendaPersister, times(0)).persist(any());
        verify(mockReadyAgendaPersister, times(0)).persist(any());
    }

    @Test
    void testReadyAgendaPersistError() throws PersistenceException
    {
        String cid = UUID.randomUUID().toString();

        Agenda agenda = getAgenda(1);

        DefaultDataObjectRequest<Agenda> request = new DefaultDataObjectRequest<>();
        request.setDataObject(agenda);
        request.setCid(cid);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());

        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setId(UUID.randomUUID().toString());
        DataObjectResponse<AgendaProgress> agendaProgressResponse = new DefaultDataObjectResponse<>();
        agendaProgressResponse.add(agendaProgress);
        doReturn(agendaProgressResponse).when(mockAgendaProgressClient).persistObject(any());

        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setId(UUID.randomUUID().toString());
        DataObjectResponse<OperationProgress> opProgressResponse = new DefaultDataObjectResponse<>();
        opProgressResponse.add(operationProgress);
        doReturn(opProgressResponse).when(mockOperationProgressClient).persistObject(any());

        doReturn(new Agenda()).when(mockAgendaPersister).persist(any());

        PersistenceException exception = new PersistenceException("Error persisting ReadyAgenda");
        doThrow(exception).when(mockReadyAgendaPersister).persist(any());

        DataObjectResponse<Agenda> response = agendaRequestProcessor.handlePOST(request);
        Assert.assertTrue(response.isError());
        ErrorResponse errorResponse = response.getErrorResponse();
        Assert.assertEquals(errorResponse.getCorrelationId(), cid);
        Assert.assertEquals(errorResponse.getTitle(), "RuntimeException");

        // verify objects were cleaned up
        verify(mockAgendaProgressClient, times(1)).deleteObject(any());
        verify(mockOperationProgressClient, times(1)).deleteObject(any());
        verify(mockAgendaPersister, times(1)).delete(any());
    }

    @Test
    void testAgendaPersistError() throws PersistenceException
    {
        String cid = UUID.randomUUID().toString();

        Agenda agenda = getAgenda(1);

        DefaultDataObjectRequest<Agenda> request = new DefaultDataObjectRequest<>();
        request.setDataObject(agenda);
        request.setCid(cid);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());

        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setId(UUID.randomUUID().toString());
        DataObjectResponse<AgendaProgress> agendaProgressResponse = new DefaultDataObjectResponse<>();
        agendaProgressResponse.add(agendaProgress);
        doReturn(agendaProgressResponse).when(mockAgendaProgressClient).persistObject(any());

        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setId(UUID.randomUUID().toString());
        DataObjectResponse<OperationProgress> opProgressResponse = new DefaultDataObjectResponse<>();
        opProgressResponse.add(operationProgress);
        doReturn(opProgressResponse).when(mockOperationProgressClient).persistObject(any());

        PersistenceException exception = new PersistenceException("Error persisting Agenda");
        doThrow(exception).when(mockAgendaPersister).persist(any());

        DataObjectResponse<Agenda> response = agendaRequestProcessor.handlePOST(request);
        Assert.assertTrue(response.isError());
        ErrorResponse errorResponse = response.getErrorResponse();
        Assert.assertEquals(errorResponse.getCorrelationId(), cid);
        Assert.assertEquals(errorResponse.getTitle(), "BadRequestException");

        // verify objects were cleaned up
        verify(mockAgendaProgressClient, times(1)).deleteObject(any());
        verify(mockOperationProgressClient, times(1)).deleteObject(any());

        // verify ReadyAgenda wasn't created
        verify(mockReadyAgendaPersister, times(0)).persist(any());
    }

    @Test
    public void testDoNotRun() throws PersistenceException
    {
        int numOps = 1;
        String param = "DoNotRun";

        Agenda agenda = getAgenda(numOps);
        ParamsMap paramsMap = new ParamsMap();
        paramsMap.put(param, true);
        agenda.setParams(paramsMap);

        AgendaProgress agendaProgressResponse = new AgendaProgress();
        agendaProgressResponse.setId(UUID.randomUUID().toString());
        DataObjectResponse<AgendaProgress> dataObjectResponse = new DefaultDataObjectResponse<>();
        dataObjectResponse.add(agendaProgressResponse);
        doReturn(dataObjectResponse).when(mockAgendaProgressClient).persistObject(any());

        doReturn(new Agenda()).when(mockAgendaPersister).persist(any());

        DataObjectResponse<OperationProgress> opProgressResponse = new DefaultDataObjectResponse<>();
        opProgressResponse.add(new OperationProgress());
        doReturn(opProgressResponse).when(mockOperationProgressClient).persistObject(any());

        DefaultDataObjectRequest<Agenda> request = new DefaultDataObjectRequest<>();
        request.setDataObject(agenda);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());
        DataObjectResponse<Agenda> response = agendaRequestProcessor.handlePOST(request);
        Assert.assertFalse(response.isError());

        verify(mockOperationProgressClient, times(numOps)).persistObject(any());
        verify(mockReadyAgendaPersister, times(0)).persist(any());
    }

    private Agenda getAgenda(int numOps)
    {
        List<Operation> ops = new ArrayList<>();
        for (int i = 1; i <= numOps; i++)
        {
            Operation operation = new Operation();
            operation.setName(RandomStringUtils.randomAlphabetic(10));
            operation.setPayload(RandomStringUtils.randomAlphanumeric(10));

            ParamsMap params = new ParamsMap();
            params.put("foo", "bar");
            operation.setParams(params);
            ops.add(operation);
        }

        Agenda agenda = new Agenda();
        agenda.setCustomerId(UUID.randomUUID().toString());
        agenda.setLinkId(UUID.randomUUID().toString());
        agenda.setJobId(UUID.randomUUID().toString());
        agenda.setOperations(ops);
        return agenda;
    }
}
