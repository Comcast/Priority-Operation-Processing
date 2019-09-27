package com.theplatform.dfh.cp.endpoint.transformrequest;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.auth.MPXAuthorizationResponseBuilder;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TransformRequestProcessorTest
{
    private final String PROGRESS_ID = "theProgressId";
    private final String EXEC_PROGRESS_ID = "theExecProgressId";
    private final String AGENDA_ID = "theAgendaId";
    private final String CUSTOMER_ID = "theCustomer";

    private TransformRequestProcessor transformRequestProcessor;
    private ObjectPersister<TransformRequest> mockTransformRequestPersister;
    private ObjectClient<AgendaProgress> mockAgendaProgressClient;
    private ObjectClient<Agenda> mockAgendaClient;
    private ObjectClient<AgendaTemplate> mockAgendaTemplateClient;

    @BeforeMethod
    public void setup()
    {
        mockTransformRequestPersister = mock(ObjectPersister.class);
        mockAgendaProgressClient = mock(ObjectClient.class);
        mockAgendaClient = mock(ObjectClient.class);
        mockAgendaTemplateClient = mock(ObjectClient.class);

        transformRequestProcessor = new TransformRequestProcessor(mockTransformRequestPersister, null, null, null, null, null, null, null);
        transformRequestProcessor.setAgendaClient(mockAgendaClient);
        transformRequestProcessor.setAgendaProgressClient(mockAgendaProgressClient);
        transformRequestProcessor.setAgendaTemplateClient(mockAgendaTemplateClient);
    }

    @Test
    public void testHandlePost() throws PersistenceException
    {
        TransformRequest transformRequest = createTransformRequest();
        
        setUpProgressMock();
        setUpAgendaMock();

        Mockito.when(mockTransformRequestPersister.persist(transformRequest)).thenReturn(transformRequest);

        DefaultDataObjectRequest<TransformRequest> request = new DefaultDataObjectRequest<>();
        request.setDataObject(transformRequest);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());
        DataObjectResponse<TransformRequest> objectPersistResponse = transformRequestProcessor.handlePOST(request);
        TransformRequest responseObject = objectPersistResponse.getFirst();
        Assert.assertEquals(responseObject.getParams().getString(GeneralParamKey.progressId), PROGRESS_ID);
        Assert.assertEquals(responseObject.getParams().getString(GeneralParamKey.execProgressId), EXEC_PROGRESS_ID);
        Assert.assertEquals(responseObject.getParams().getString(GeneralParamKey.agendaId), AGENDA_ID);
    }

    @Test
    void testAgendaProgressPersistError() throws PersistenceException
    {
        TransformRequest transformRequest = createTransformRequest();
        Mockito.when(mockTransformRequestPersister.persist(transformRequest)).thenReturn(transformRequest);

        String cid = UUID.randomUUID().toString();
        DataObjectResponse<AgendaProgress> agendaProgressResponse = new DefaultDataObjectResponse<>();
        agendaProgressResponse.setErrorResponse(ErrorResponseFactory.unauthorized("Unauthorized request.", cid));
        doReturn(agendaProgressResponse).when(mockAgendaProgressClient).persistObject(any());

        DefaultDataObjectRequest<TransformRequest> request = new DefaultDataObjectRequest<>();
        request.setDataObject(transformRequest);
        request.setCid(cid);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());

        DataObjectResponse<TransformRequest> objectPersistResponse = transformRequestProcessor.handlePOST(request);
        Assert.assertTrue(objectPersistResponse.isError());
        ErrorResponse errorResponse = objectPersistResponse.getErrorResponse();
        Assert.assertEquals(errorResponse.getCorrelationId(), cid);
        Assert.assertEquals(errorResponse.getResponseCode(), agendaProgressResponse.getErrorResponse().getResponseCode());
        Assert.assertEquals(errorResponse.getTitle(), agendaProgressResponse.getErrorResponse().getTitle());

        verify(mockTransformRequestPersister, times(1)).delete(any());
    }

    @Test
    void testAgendaPersistError() throws PersistenceException
    {
        String cid = UUID.randomUUID().toString();

        TransformRequest transformRequest = createTransformRequest();
        Mockito.when(mockTransformRequestPersister.persist(transformRequest)).thenReturn(transformRequest);

        setUpProgressMock();

        DataObjectResponse<Agenda> agendaResponse = new DefaultDataObjectResponse<>();
        agendaResponse.setErrorResponse(ErrorResponseFactory.badRequest("Bad request.", cid));
        doReturn(agendaResponse).when(mockAgendaClient).persistObject(any());

        DefaultDataObjectRequest<TransformRequest> request = new DefaultDataObjectRequest<>();
        request.setDataObject(transformRequest);
        request.setCid(cid);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());

        DataObjectResponse<TransformRequest> objectPersistResponse = transformRequestProcessor.handlePOST(request);
        Assert.assertTrue(objectPersistResponse.isError());
        ErrorResponse errorResponse = objectPersistResponse.getErrorResponse();
        Assert.assertEquals(errorResponse.getCorrelationId(), cid);
        Assert.assertEquals(errorResponse.getResponseCode(), agendaResponse.getErrorResponse().getResponseCode());
        Assert.assertEquals(errorResponse.getTitle(), "RuntimeException");

        verify(mockTransformRequestPersister, times(1)).delete(any());
        verify(mockAgendaProgressClient, times(2)).deleteObject(any());
    }

    @Test
    void testTransformRequestPersistError() throws PersistenceException
    {
        String cid = UUID.randomUUID().toString();
        TransformRequest transformRequest = createTransformRequest();

        PersistenceException persistenceException = new PersistenceException("Failed to persist TransformRequest.");
        doThrow(persistenceException).when(mockTransformRequestPersister).persist(any());

        DefaultDataObjectRequest<TransformRequest> request = new DefaultDataObjectRequest<>();
        request.setDataObject(transformRequest);
        request.setCid(cid);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());

        DataObjectResponse<TransformRequest> objectPersistResponse = transformRequestProcessor.handlePOST(request);
        Assert.assertTrue(objectPersistResponse.isError());
        ErrorResponse errorResponse = objectPersistResponse.getErrorResponse();
        Assert.assertEquals(errorResponse.getCorrelationId(), cid);
        Assert.assertEquals(errorResponse.getTitle(), "BadRequestException");
    }

    protected TransformRequest createTransformRequest()
    {
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setCustomerId(CUSTOMER_ID);
        return transformRequest;
    }

    private void setUpProgressMock()
    {
        // NOTE: If the order of the creates changes this will break
        doAnswer(new Answer()
        {
            private Integer callCount = 0;

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                AgendaProgress result = new AgendaProgress();
                result.setId(callCount > 0 ? EXEC_PROGRESS_ID : PROGRESS_ID);
                callCount++;
                DataObjectResponse<AgendaProgress> dataObjectResponse = new DefaultDataObjectResponse<>();
                dataObjectResponse.add(result);
                return dataObjectResponse;
            }
        }).when(mockAgendaProgressClient).persistObject(any());
    }

    private void setUpAgendaMock()
    {
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Agenda agenda = (Agenda)invocationOnMock.getArguments()[0];
                Agenda response =  new Agenda();
                response.setId(AGENDA_ID);
                ParamsMap paramsMap = new ParamsMap();
                paramsMap.put(GeneralParamKey.progressId, PROGRESS_ID);
                response.setParams(paramsMap);
                DataObjectResponse<Agenda> dataObjectResponse = new DefaultDataObjectResponse<>();
                dataObjectResponse.add(response);
                return dataObjectResponse;
            }
        }).when(mockAgendaClient).persistObject(any());
    }
}
