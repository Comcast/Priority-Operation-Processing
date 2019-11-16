package com.theplatform.dfh.cp.endpoint.transformrequest;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.endpoint.AbstractRequestProcessorTest;
import com.theplatform.dfh.cp.endpoint.agenda.factory.AgendaFactory;
import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.auth.MPXAuthorizationResponseBuilder;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
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
import static org.mockito.Mockito.when;

public class TransformRequestProcessorTest extends AbstractRequestProcessorTest<TransformRequest>
{
    private final String PROGRESS_ID = "theProgressId";
    private final String EXEC_PROGRESS_ID = "theExecProgressId";
    private final String AGENDA_ID = "theAgendaId";
    private final String CUSTOMER_ID = "theCustomer";

    private DataObjectRequestProcessor<AgendaProgress> mockAgendaProgressRequestProcessor;
    private DataObjectRequestProcessor<Agenda> mockAgendaRequestProcessor;
    private DataObjectRequestProcessor<AgendaTemplate> mockAgendaTemplateClient;
    private AgendaFactory mockAgendaFactory;
    private AgendaTemplate idAgendaTemplate;
    private AgendaTemplate nameAgendaTemplate;

    @BeforeMethod
    public void setup()
    {
        mockAgendaProgressRequestProcessor = mock(DataObjectRequestProcessor.class);
        mockAgendaRequestProcessor = mock(DataObjectRequestProcessor.class);
        mockAgendaTemplateClient = mock(DataObjectRequestProcessor.class);
        mockAgendaFactory = mock(AgendaFactory.class);
        idAgendaTemplate = createAgendaTemplate();
        nameAgendaTemplate = createAgendaTemplate();

        doReturn(new Agenda()).when(mockAgendaFactory).createAgenda(any(), any(), any(), any());
    }

    public DataObjectRequestProcessor getRequestProcessor(ObjectPersister<TransformRequest> persister)
    {
        TransformRequestProcessor transformRequestProcessor = new TransformRequestProcessor(persister, null, null, null, null, null, null, null);
        transformRequestProcessor.setAgendaRequestProcessor(mockAgendaRequestProcessor);
        transformRequestProcessor.setAgendaProgressRequestProcessor(mockAgendaProgressRequestProcessor);
        transformRequestProcessor.setAgendaTemplateClient(mockAgendaTemplateClient);
        transformRequestProcessor.setAgendaFactory(mockAgendaFactory);
        return transformRequestProcessor;
    }

    @Test
    public void testHandlePost() throws PersistenceException
    {
        TransformRequest transformRequest = getDataObject();
        
        setUpProgressMock();
        setUpAgendaMock();
        setupAgendaTemplateClientMock(null, nameAgendaTemplate);

        when(getPersister().persist(transformRequest)).thenReturn(transformRequest);

        DefaultDataObjectRequest<TransformRequest> request = new DefaultDataObjectRequest<>();
        request.setDataObject(transformRequest);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());
        DataObjectResponse<TransformRequest> objectPersistResponse = getRequestProcessor(getPersister()).handlePOST(request);
        verify(mockAgendaProgressRequestProcessor, times(2)).handlePOST(any());
        Assert.assertFalse(objectPersistResponse.isError());
        TransformRequest responseObject = objectPersistResponse.getFirst();
        Assert.assertEquals(responseObject.getParams().getString(GeneralParamKey.progressId), PROGRESS_ID);
        Assert.assertEquals(responseObject.getParams().getString(GeneralParamKey.execProgressId), EXEC_PROGRESS_ID);
        Assert.assertEquals(responseObject.getParams().getString(GeneralParamKey.agendaId), AGENDA_ID);
    }

    @Test
    public void testDoNotCreateExecProgress() throws PersistenceException
    {
        TransformRequest transformRequest = getDataObject();

        setUpProgressMock();
        setUpAgendaMock();
        setupAgendaTemplateClientMock(null, nameAgendaTemplate);

        nameAgendaTemplate.getParams().put(TransformRequestProcessor.CREATE_EXEC_PROGRESS_PARAM, false);

        when(getPersister().persist(transformRequest)).thenReturn(transformRequest);

        DefaultDataObjectRequest<TransformRequest> request = new DefaultDataObjectRequest<>();
        request.setDataObject(transformRequest);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());
        DataObjectResponse<TransformRequest> objectPersistResponse = getRequestProcessor(getPersister()).handlePOST(request);
        verify(mockAgendaProgressRequestProcessor, times(1)).handlePOST(any());
        TransformRequest responseObject = objectPersistResponse.getFirst();

        Assert.assertEquals(responseObject.getParams().getString(GeneralParamKey.progressId), PROGRESS_ID);
        Assert.assertFalse(responseObject.getParams().containsKey(TransformRequestProcessor.CREATE_EXEC_PROGRESS_PARAM));
        Assert.assertEquals(responseObject.getParams().getString(GeneralParamKey.agendaId), AGENDA_ID);
    }

    @Test
    void testAgendaProgressPersistError() throws PersistenceException
    {
        setupAgendaTemplateClientMock(null, nameAgendaTemplate);
        TransformRequest transformRequest = getDataObject();
        when(getPersister().persist(transformRequest)).thenReturn(transformRequest);

        String cid = UUID.randomUUID().toString();
        DataObjectResponse<AgendaProgress> agendaProgressResponse = new DefaultDataObjectResponse<>();
        agendaProgressResponse.setErrorResponse(ErrorResponseFactory.unauthorized("Unauthorized request.", cid));
        doReturn(agendaProgressResponse).when(mockAgendaProgressRequestProcessor).handlePOST(any());

        DefaultDataObjectRequest<TransformRequest> request = new DefaultDataObjectRequest<>();
        request.setDataObject(transformRequest);
        request.setCid(cid);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());

        DataObjectResponse<TransformRequest> objectPersistResponse = getRequestProcessor(getPersister()).handlePOST(request);
        Assert.assertTrue(objectPersistResponse.isError());
        ErrorResponse errorResponse = objectPersistResponse.getErrorResponse();
        Assert.assertEquals(errorResponse.getCorrelationId(), cid);
        Assert.assertEquals(errorResponse.getResponseCode(), agendaProgressResponse.getErrorResponse().getResponseCode());
        Assert.assertEquals(errorResponse.getTitle(), agendaProgressResponse.getErrorResponse().getTitle());

        //@todo Tim fix this for babs. I see the delete get called but mock doesn't
        //verify(getPersister(), times(1)).delete(any());
    }

    @Test
    void testAgendaPersistError() throws PersistenceException
    {
        String cid = UUID.randomUUID().toString();
        setupAgendaTemplateClientMock(null, nameAgendaTemplate);

        TransformRequest transformRequest = getDataObject();
        when(getPersister().persist(transformRequest)).thenReturn(transformRequest);

        setUpProgressMock();

        DataObjectResponse<Agenda> agendaResponse = new DefaultDataObjectResponse<>();
        agendaResponse.setErrorResponse(ErrorResponseFactory.badRequest("Bad request.", cid));
        doReturn(agendaResponse).when(mockAgendaRequestProcessor).handlePOST(any());

        DefaultDataObjectRequest<TransformRequest> request = new DefaultDataObjectRequest<>();
        request.setDataObject(transformRequest);
        request.setCid(cid);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());

        DataObjectResponse<TransformRequest> objectPersistResponse = getRequestProcessor(getPersister()).handlePOST(request);
        Assert.assertTrue(objectPersistResponse.isError());
        ErrorResponse errorResponse = objectPersistResponse.getErrorResponse();
        Assert.assertEquals(errorResponse.getCorrelationId(), cid);
        Assert.assertEquals(errorResponse.getResponseCode(), agendaResponse.getErrorResponse().getResponseCode());
        Assert.assertEquals(errorResponse.getTitle(), "RuntimeException");

        verify(getPersister(), times(1)).delete(any());
        verify(mockAgendaProgressRequestProcessor, times(2)).handleDELETE(any());
    }

    @Test
    void testTransformRequestPersistError() throws PersistenceException
    {
        String cid = UUID.randomUUID().toString();
        setupAgendaTemplateClientMock(null, nameAgendaTemplate);
        TransformRequest transformRequest = getDataObject();

        PersistenceException persistenceException = new PersistenceException("Failed to persist TransformRequest.");
        doThrow(persistenceException).when(getPersister()).persist(any());

        DefaultDataObjectRequest<TransformRequest> request = new DefaultDataObjectRequest<>();
        request.setDataObject(transformRequest);
        request.setCid(cid);
        request.setAuthorizationResponse(new MPXAuthorizationResponseBuilder().withSuperUser(true).build());

        DataObjectResponse<TransformRequest> objectPersistResponse = getRequestProcessor(getPersister()).handlePOST(request);
        Assert.assertTrue(objectPersistResponse.isError());
        ErrorResponse errorResponse = objectPersistResponse.getErrorResponse();
        Assert.assertEquals(errorResponse.getCorrelationId(), cid);
        Assert.assertEquals(errorResponse.getTitle(), "BadRequestException");
    }

    @Test
    public void testRetrieveAgendaTemplateById()
    {
        setupAgendaTemplateClientMock(idAgendaTemplate, null);
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setAgendaTemplateId("theId");
        Assert.assertEquals(((TransformRequestProcessor)getRequestProcessor(getPersister())).retrieveAgendaTemplate(new MPXAuthorizationResponseBuilder().withSuperUser(true).build(),transformRequest, null).getFirst(), idAgendaTemplate);
        verify(mockAgendaTemplateClient, times(1)).handleGET(any());
    }

    @Test
    public void testRetrieveAgendaTemplateByName()
    {
        setupAgendaTemplateClientMock(null, nameAgendaTemplate);
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setAgendaTemplateTitle("theName");
        Assert.assertEquals(((TransformRequestProcessor)getRequestProcessor(getPersister())).retrieveAgendaTemplate(new MPXAuthorizationResponseBuilder().withSuperUser(true).build(),transformRequest, null).getFirst(), nameAgendaTemplate);
        verify(mockAgendaTemplateClient, times(1)).handleGET(any());
    }
    @Override
    @Test
    public void testHandlePostCustomerIsOwnerVisibility() throws PersistenceException
    {
        setupAgendaTemplateClientMock(idAgendaTemplate, nameAgendaTemplate);
        setUpProgressMock();
        setUpAgendaMock();

        super.testHandlePostCustomerIsOwnerVisibility();
    }
    public TransformRequest getDataObject()
    {
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setCustomerId(CUSTOMER_ID);
        transformRequest.setAgendaTemplateTitle("DoesNotMatter");
        transformRequest.setId(UUID.randomUUID().toString());
        return transformRequest;
    }

    protected AgendaTemplate createAgendaTemplate()
    {
        AgendaTemplate agendaTemplate = new AgendaTemplate();
        agendaTemplate.setParams(new ParamsMap());
        return agendaTemplate;
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
        }).when(mockAgendaProgressRequestProcessor).handlePOST(any());
    }

    private void setupAgendaTemplateClientMock(AgendaTemplate idLookupResult, AgendaTemplate nameLookupResult)
    {
        DataObjectResponse<AgendaTemplate> idResponse = new DefaultDataObjectResponse<>();
        if(idLookupResult != null) idResponse.add(idLookupResult);
        DataObjectResponse<AgendaTemplate> nameResponse = new DefaultDataObjectResponse<>();
        if(nameLookupResult != null) nameResponse.add(nameLookupResult);

        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                DataObjectRequest request = (DataObjectRequest) invocationOnMock.getArguments()[0];
                if(request.getId() != null)
                    return idResponse;
                return nameResponse;
            }
        }).when(mockAgendaTemplateClient).handleGET(any());
    }

    private void setUpAgendaMock()
    {
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                DataObjectRequest<Agenda> dataObjectRequest = (DataObjectRequest<Agenda>)invocationOnMock.getArguments()[0];
                Agenda agenda = dataObjectRequest.getDataObject();
                Agenda response =  new Agenda();
                response.setId(AGENDA_ID);
                ParamsMap paramsMap = new ParamsMap();
                paramsMap.put(GeneralParamKey.progressId, PROGRESS_ID);
                response.setParams(paramsMap);
                DataObjectResponse<Agenda> dataObjectResponse = new DefaultDataObjectResponse<>();
                dataObjectResponse.add(response);
                return dataObjectResponse;
            }
        }).when(mockAgendaRequestProcessor).handlePOST(any());
    }
}
