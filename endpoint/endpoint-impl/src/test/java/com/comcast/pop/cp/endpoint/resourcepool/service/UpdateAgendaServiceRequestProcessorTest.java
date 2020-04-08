package com.comcast.pop.cp.endpoint.resourcepool.service;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaInsight;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.cp.endpoint.agenda.AgendaRequestProcessor;
import com.comcast.pop.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.comcast.pop.cp.endpoint.util.ServiceDataObjectRetriever;
import com.comcast.pop.cp.endpoint.util.ServiceDataRequestResult;
import com.comcast.pop.cp.endpoint.factory.RequestProcessorFactory;
import com.comcast.pop.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.comcast.pop.cp.endpoint.resourcepool.InsightRequestProcessor;
import com.comcast.pop.endpoint.api.DefaultServiceRequest;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.RuntimeServiceException;
import com.comcast.pop.endpoint.api.agenda.UpdateAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.UpdateAgendaResponse;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class UpdateAgendaServiceRequestProcessorTest
{
    private UpdateAgendaServiceRequestProcessor requestProcessor;
    private UpdateAgendaRequest updateAgendaRequest;
    private DefaultServiceRequest<UpdateAgendaRequest> serviceRequest;

    private RequestProcessorFactory mockRequestProcessorFactory;
    private AgendaRequestProcessor mockAgendaRequestProcessor;
    private AgendaProgressRequestProcessor mockAgendaProgressRequestProcessor;
    private OperationProgressRequestProcessor mockOperationProgressRequestProcessor;
    private ServiceDataObjectRetriever<UpdateAgendaResponse> mockDataObjectRetriever;

    @BeforeMethod
    public void setup()
    {
        updateAgendaRequest = new UpdateAgendaRequest();
        updateAgendaRequest.setAgendaId("");
        updateAgendaRequest.setOperations(createOperations(new String[] { "newOp" }));
        serviceRequest = new DefaultServiceRequest<>(updateAgendaRequest);

        mockRequestProcessorFactory = mock(RequestProcessorFactory.class);
        mockAgendaRequestProcessor = mock(AgendaRequestProcessor.class);
        mockAgendaProgressRequestProcessor = mock(AgendaProgressRequestProcessor.class);
        mockOperationProgressRequestProcessor = mock(OperationProgressRequestProcessor.class);
        mockDataObjectRetriever = mock(ServiceDataObjectRetriever.class);
        doReturn(mockAgendaRequestProcessor).when(mockRequestProcessorFactory).createAgendaRequestProcessor(
            any(), any(), any(), any(), any(), any());
        doReturn(mockOperationProgressRequestProcessor).when(mockRequestProcessorFactory).createOperationProgressRequestProcessor(any());
        doReturn(mockAgendaProgressRequestProcessor).when(mockRequestProcessorFactory).createAgendaProgressRequestProcessor(any(), any(), any());
        requestProcessor = new UpdateAgendaServiceRequestProcessor(null, null, null, null, null, null);
        requestProcessor.setRequestProcessorFactory(mockRequestProcessorFactory);
        requestProcessor.setServiceDataObjectRetriever(mockDataObjectRetriever);
    }

    @DataProvider
    public Object[][] validParamsMapProvider()
    {
        return new Object[][]
        {
            {null},
            {new ParamsMap()},
            {createParamsMap("key1")},
            {createParamsMap("key1", "key2", "key3")},
        };
    }

    @Test(dataProvider = "validParamsMapProvider")
    public void testSuccessfulUpdate(ParamsMap paramsMap)
    {
        setupSuccessfulAgendaLookup();
        setupSuccessfulInsightLookup();
        setupSuccessfulAgendaProgressLookup();
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                DataObjectRequest<Agenda> agendaRequest = (DataObjectRequest<Agenda>)invocation.getArguments()[0];
                DataObjectResponse<Agenda> agendaResponse = new DefaultDataObjectResponse<>();
                agendaResponse.add(agendaRequest.getDataObject());
                return agendaResponse;
            }
        }).when(mockAgendaRequestProcessor).handlePUT(any());

        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaProgressRequestProcessor).handlePUT(any());
        doReturn(new DefaultDataObjectResponse<>()).when(mockOperationProgressRequestProcessor).handlePOST(any());
        if(paramsMap != null)
            updateAgendaRequest.setParams(paramsMap);
        UpdateAgendaResponse response = testExecute(1,1);
        Assert.assertNotNull(response);
        Agenda resultingAgenda = response.getAgenda();
        Assert.assertNotNull(resultingAgenda);
        if(paramsMap != null)
        {
            ParamsMap resultingMap = resultingAgenda.getParams();
            Assert.assertNotNull(resultingMap);
            for (Map.Entry<String, Object> entries : paramsMap.entrySet())
            {
                Assert.assertTrue(resultingMap.containsKey(entries.getKey()));
                Assert.assertEquals(resultingMap.get(entries.getKey()), entries.getValue());
            }
        }
        else
        {
            Assert.assertNull(resultingAgenda.getParams());
        }
    }

    @Test
    public void testErrorOnAgendaLookup()
    {
        UpdateAgendaResponse updateAgendaResponse = new UpdateAgendaResponse();
        updateAgendaResponse.setErrorResponse(ErrorResponseFactory.objectNotFound("", null));
        ServiceDataRequestResult<Agenda, UpdateAgendaResponse> agendaResult = new ServiceDataRequestResult<>();
        agendaResult.setServiceResponse(updateAgendaResponse);
        doReturn(agendaResult).when(mockDataObjectRetriever).performObjectRetrieve(any(), any(), any(), any(), eq(Agenda.class));
        testErrorExecute(0, 0);
    }

    @Test
    public void testErrorOnInsightLookup()
    {
        setupSuccessfulAgendaLookup();
        UpdateAgendaResponse updateAgendaResponse = new UpdateAgendaResponse();
        updateAgendaResponse.setErrorResponse(ErrorResponseFactory.objectNotFound("", null));
        ServiceDataRequestResult<Insight, UpdateAgendaResponse> insightResult = new ServiceDataRequestResult<>();
        insightResult.setServiceResponse(updateAgendaResponse);
        doReturn(insightResult).when(mockDataObjectRetriever).performObjectRetrieve(any(), any(InsightRequestProcessor.class), any(), eq(Insight.class));
        testErrorExecute(0, 0);
    }

    @Test
    public void testErrorOnAgendaProgressLookup()
    {
        setupSuccessfulAgendaLookup();
        setupSuccessfulInsightLookup();
        UpdateAgendaResponse updateAgendaResponse = new UpdateAgendaResponse();
        updateAgendaResponse.setErrorResponse(ErrorResponseFactory.objectNotFound("", null));
        ServiceDataRequestResult<AgendaProgress, UpdateAgendaResponse> agendaProgressResult = new ServiceDataRequestResult<>();
        agendaProgressResult.setServiceResponse(updateAgendaResponse);
        doReturn(agendaProgressResult).when(mockDataObjectRetriever).performObjectRetrieve(any(), any(), any(), any(), eq(AgendaProgress.class));
        testErrorExecute(0, 0);
    }

    @Test
    public void testErrorOnAgendaUpdate()
    {
        setupSuccessfulAgendaLookup();
        setupSuccessfulInsightLookup();
        setupSuccessfulAgendaProgressLookup();
        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaProgressRequestProcessor).handlePUT(any());
        DefaultDataObjectResponse<Agenda> agendaPersistResponse = new DefaultDataObjectResponse<>();
        agendaPersistResponse.setErrorResponse(ErrorResponseFactory.runtimeServiceException("", null));
        doReturn(agendaPersistResponse).when(mockAgendaRequestProcessor).handlePUT(any());
        testErrorExecute(1, 0);
    }

    @Test
    public void testExceptionOnAgendaUpdate()
    {
        setupSuccessfulAgendaLookup();
        setupSuccessfulInsightLookup();
        setupSuccessfulAgendaProgressLookup();
        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaProgressRequestProcessor).handlePUT(any());
        DefaultDataObjectResponse<Agenda> agendaPersistResponse = new DefaultDataObjectResponse<>();
        agendaPersistResponse.setErrorResponse(ErrorResponseFactory.runtimeServiceException("", null));
        doThrow(new RuntimeServiceException("", 500)).when(mockAgendaRequestProcessor).handlePUT(any());
        testErrorExecute(1, 0);
    }

    @Test
    public void testErrorOnOperationProgressCreate()
    {
        setupSuccessfulAgendaLookup();
        setupSuccessfulInsightLookup();
        setupSuccessfulAgendaProgressLookup();
        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaProgressRequestProcessor).handlePUT(any());
        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaRequestProcessor).handlePUT(any());
        DefaultDataObjectResponse<OperationProgress> operationProgressCreateResponse = new DefaultDataObjectResponse<>();
        operationProgressCreateResponse.setErrorResponse(ErrorResponseFactory.runtimeServiceException("", null));
        doReturn(operationProgressCreateResponse).when(mockOperationProgressRequestProcessor).handlePOST(any());
        testErrorExecute(1, 1);
    }

    @Test
    public void testExceptionOnOperationProgressCreate()
    {
        setupSuccessfulAgendaLookup();
        setupSuccessfulInsightLookup();
        setupSuccessfulAgendaProgressLookup();
        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaProgressRequestProcessor).handlePUT(any());
        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaRequestProcessor).handlePUT(any());
        doThrow(new RuntimeServiceException("", 500)).when(mockOperationProgressRequestProcessor).handlePOST(any());
        testErrorExecute(1, 1);
    }

    private void setupSuccessfulAgendaLookup()
    {
        DataObjectResponse<Agenda> agendaResponse = new DefaultDataObjectResponse<>();
        agendaResponse.add(createAgenda(new String[]{"existingOp"}));
        ServiceDataRequestResult<Agenda, UpdateAgendaResponse> result = new ServiceDataRequestResult<>();
        result.setDataObjectResponse(agendaResponse);
        doReturn(result).when(mockDataObjectRetriever).performObjectRetrieve(any(), any(), any(), any(), eq(Agenda.class));
    }

    private void setupSuccessfulInsightLookup()
    {
        ServiceDataRequestResult<Insight, UpdateAgendaResponse> result = new ServiceDataRequestResult<>();
        result.setDataObjectResponse(new DefaultDataObjectResponse<>());
        // matching on these is not obvious, but eq + the request processsor did it!
        doReturn(result).when(mockDataObjectRetriever).performObjectRetrieve(any(), any(InsightRequestProcessor.class), any(), eq(Insight.class));
    }

    private void setupSuccessfulAgendaProgressLookup()
    {
        DataObjectResponse<AgendaProgress> agendaProgressResponse = new DefaultDataObjectResponse<>();
        ServiceDataRequestResult<AgendaProgress, UpdateAgendaResponse> result = new ServiceDataRequestResult<>();
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgressResponse.add(agendaProgress);
        result.setDataObjectResponse(agendaProgressResponse);
        doReturn(result).when(mockDataObjectRetriever).performObjectRetrieve(any(), any(), any(), any(), eq(AgendaProgress.class));
    }

    private void testErrorExecute(int expectedAgendaPUTs, int expectedOperationProgressPOSTs)
    {
        UpdateAgendaResponse response = testExecute(expectedAgendaPUTs, expectedOperationProgressPOSTs);
        Assert.assertNotNull(response);
        Assert.assertTrue(response.isError());
    }

    private UpdateAgendaResponse testExecute(int expectedAgendaPUTs, int expectedOperationProgressPOSTs)
    {
        try
        {
            UpdateAgendaResponse response = requestProcessor.processPOST(serviceRequest);
            verify(mockAgendaRequestProcessor, times(expectedAgendaPUTs)).handlePUT(any());
            verify(mockOperationProgressRequestProcessor, times(expectedOperationProgressPOSTs)).handlePOST(any());
            return response;
        }
        catch(Throwable t)
        {
            Assert.fail("Test failed for an unknown reason.", t);
        }
        return null;
    }

    private Agenda createAgenda(String[] newOperationNames)
    {
        Agenda agenda = new Agenda();
        if(newOperationNames != null)
        {
            agenda.setOperations(createOperations(newOperationNames));
        }
        agenda.setAgendaInsight(new AgendaInsight());
        return agenda;
    }

    private List<Operation> createOperations(String[] operationNames)
    {
        return Arrays.stream(operationNames).map(opName ->
        {
            Operation op = new Operation();
            op.setName(opName);
            return op;
        }).collect(Collectors.toList());
    }

    private ParamsMap createParamsMap(String... keyNames)
    {
        ParamsMap paramsMap = new ParamsMap();
        Arrays.stream(keyNames).forEach(key ->
        {
            paramsMap.put(key, UUID.randomUUID().toString());
        });
        return paramsMap;
    }
}
