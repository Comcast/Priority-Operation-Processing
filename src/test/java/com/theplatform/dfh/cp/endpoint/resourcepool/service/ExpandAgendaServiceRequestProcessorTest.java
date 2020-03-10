package com.theplatform.dfh.cp.endpoint.resourcepool.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaInsight;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.factory.RequestProcessorFactory;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.util.ServiceDataObjectRetriever;
import com.theplatform.dfh.cp.endpoint.util.ServiceDataRequestResult;
import com.theplatform.dfh.endpoint.api.DefaultServiceRequest;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.RuntimeServiceException;
import com.theplatform.dfh.endpoint.api.agenda.service.ExpandAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.ExpandAgendaResponse;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ExpandAgendaServiceRequestProcessorTest
{
    private ExpandAgendaServiceRequestProcessor requestProcessor;
    private ExpandAgendaRequest expandAgendaRequest;

    private RequestProcessorFactory mockRequestProcessorFactory;
    private AgendaRequestProcessor mockAgendaRequestProcessor;
    private OperationProgressRequestProcessor mockOperationProgressRequestProcessor;
    private ServiceDataObjectRetriever<ExpandAgendaResponse> mockDataObjectRetriever;

    @BeforeMethod
    public void setup()
    {
        expandAgendaRequest = new ExpandAgendaRequest();
        expandAgendaRequest.setAgendaId("");
        expandAgendaRequest.setOperations(createOperations(new String[] { "newOp" }));

        mockRequestProcessorFactory = mock(RequestProcessorFactory.class);
        mockAgendaRequestProcessor = mock(AgendaRequestProcessor.class);
        mockOperationProgressRequestProcessor = mock(OperationProgressRequestProcessor.class);
        mockDataObjectRetriever = mock(ServiceDataObjectRetriever.class);
        doReturn(mockAgendaRequestProcessor).when(mockRequestProcessorFactory).createAgendaRequestProcessor(
            any(), any(), any(), any(), any(), any());
        doReturn(mockOperationProgressRequestProcessor).when(mockRequestProcessorFactory).createOperationProgressRequestProcessor(any());
        requestProcessor = new ExpandAgendaServiceRequestProcessor(null, null, null, null, null, null);
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

        doReturn(new DefaultDataObjectResponse<>()).when(mockOperationProgressRequestProcessor).handlePOST(any());
        if(paramsMap != null)
            expandAgendaRequest.setParams(paramsMap);
        ExpandAgendaResponse response = testExecute(1,1);
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
        ExpandAgendaResponse expandAgendaResponse = new ExpandAgendaResponse();
        expandAgendaResponse.setErrorResponse(ErrorResponseFactory.objectNotFound("", null));
        ServiceDataRequestResult<Agenda, ExpandAgendaResponse> agendaResult = new ServiceDataRequestResult<>();
        agendaResult.setServiceResponse(expandAgendaResponse);
        doReturn(agendaResult).when(mockDataObjectRetriever).performObjectRetrieve(any(), any(), any(), any(), any());
        testErrorExecute(0, 0);
    }

    @Test
    public void testErrorOnInsightLookup()
    {
        setupSuccessfulAgendaLookup();
        ExpandAgendaResponse expandAgendaResponse = new ExpandAgendaResponse();
        expandAgendaResponse.setErrorResponse(ErrorResponseFactory.objectNotFound("", null));
        ServiceDataRequestResult<Insight, ExpandAgendaResponse> agendaResult = new ServiceDataRequestResult<>();
        agendaResult.setServiceResponse(expandAgendaResponse);
        doReturn(agendaResult).when(mockDataObjectRetriever).performObjectRetrieve(any(), any(), any(), any());
        testErrorExecute(0, 0);
    }

    @Test
    public void testErrorOnAgendaUpdate()
    {
        setupSuccessfulAgendaLookup();
        setupSuccessfulInsightLookup();
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
        DefaultDataObjectResponse<OperationProgress> operationProgressCreateResponse = new DefaultDataObjectResponse<>();
        operationProgressCreateResponse.setErrorResponse(ErrorResponseFactory.runtimeServiceException("", null));
        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaRequestProcessor).handlePUT(any());
        doReturn(operationProgressCreateResponse).when(mockOperationProgressRequestProcessor).handlePOST(any());
        testErrorExecute(1, 1);
    }

    @Test
    public void testExceptionOnOperationProgressCreate()
    {
        setupSuccessfulAgendaLookup();
        setupSuccessfulInsightLookup();
        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaRequestProcessor).handlePUT(any());
        doThrow(new RuntimeServiceException("", 500)).when(mockOperationProgressRequestProcessor).handlePOST(any());
        testErrorExecute(1, 1);
    }

    private void setupSuccessfulAgendaLookup()
    {
        DataObjectResponse<Agenda> agendaResponse = new DefaultDataObjectResponse<>();
        agendaResponse.add(createAgenda(new String[]{"existingOp"}));
        ServiceDataRequestResult<Agenda, ExpandAgendaResponse> result = new ServiceDataRequestResult<>();
        result.setDataObjectResponse(agendaResponse);
        // TODO: this happens to match with the Agenda lookup
        doReturn(result).when(mockDataObjectRetriever).performObjectRetrieve(any(), any(), any(), any(), any());
    }

    private void setupSuccessfulInsightLookup()
    {
        ServiceDataRequestResult<Insight, ExpandAgendaResponse> result = new ServiceDataRequestResult<>();
        result.setDataObjectResponse(new DefaultDataObjectResponse<>());
        // TODO: this happens to match with the Insight lookup
        doReturn(result).when(mockDataObjectRetriever).performObjectRetrieve(any(), any(), any(), any());
    }

    private void testErrorExecute(int expectedAgendaPUTs, int expectedOperationProgressPOSTs)
    {
        ExpandAgendaResponse response = testExecute(expectedAgendaPUTs, expectedOperationProgressPOSTs);
        Assert.assertNotNull(response);
        Assert.assertTrue(response.isError());
    }

    private ExpandAgendaResponse testExecute(int expectedAgendaPUTs, int expectedOperationProgressPOSTs)
    {
        try
        {
            ExpandAgendaResponse response = requestProcessor.processPOST(new DefaultServiceRequest<>(expandAgendaRequest));
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
