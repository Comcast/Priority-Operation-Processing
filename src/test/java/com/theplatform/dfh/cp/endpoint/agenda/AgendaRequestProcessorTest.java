package com.theplatform.dfh.cp.endpoint.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.operation.Operation;
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

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
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
        Agenda agenda = getAgenda();

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

        verify(mockOperationProgressClient, times(2)).persistObject(any());
    }

    @Test
    void testInsightNotFound() throws PersistenceException
    {
        doReturn(null).when(mockInsightSelector).select(any());

        DefaultDataObjectRequest<Agenda> request = new DefaultDataObjectRequest<>();
        request.setDataObject(getAgenda());
        request.setCid(UUID.randomUUID().toString());

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
    void testAgendaProgressError() throws PersistenceException
    {
        String cid = UUID.randomUUID().toString();
        DataObjectResponse<AgendaProgress> agendaProgressResponse = new DefaultDataObjectResponse<>();
        agendaProgressResponse.setErrorResponse(ErrorResponseFactory.unauthorized("Unauthorized request.", cid));
        doReturn(agendaProgressResponse).when(mockAgendaProgressClient).persistObject(any());

        DefaultDataObjectRequest<Agenda> request = new DefaultDataObjectRequest<>();
        request.setDataObject(getAgenda());
        request.setCid(cid);

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

    Agenda getAgenda()
    {
        Operation operation1 = new Operation();
        operation1.setName(RandomStringUtils.randomAlphabetic(10));
        operation1.setPayload(RandomStringUtils.randomAlphanumeric(10));

        Operation operation2 = new Operation();
        operation2.setName(RandomStringUtils.randomAlphabetic(10));
        operation2.setPayload(RandomStringUtils.randomAlphanumeric(10));

        Agenda agenda = new Agenda();
        agenda.setCustomerId(UUID.randomUUID().toString());
        agenda.setLinkId(UUID.randomUUID().toString());
        agenda.setJobId(UUID.randomUUID().toString());
        agenda.setOperations(Arrays.asList(operation1, operation2));
        return agenda;
    }
}
