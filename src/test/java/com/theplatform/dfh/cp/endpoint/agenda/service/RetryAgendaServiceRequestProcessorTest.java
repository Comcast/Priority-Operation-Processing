package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.TestUtil;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.factory.RequestProcessorFactory;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.DefaultServiceRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaParameter;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RetryAgendaServiceRequestProcessorTest
{
    final String ERROR_MESSAGE = "theError";

    private RetryAgendaServiceRequestProcessor requestProcessor;

    private RetryAgendaRequest retryAgendaRequest;
    private Agenda agenda;

    private RequestProcessorFactory mockRequestProcessorFactory;
    private AgendaRequestProcessor mockAgendaRequestProcessor;
    private AgendaProgressRequestProcessor mockAgendaProgressRequestProcessor;
    private ObjectPersister<ReadyAgenda> mockReadyAgendaPersister;
    private ObjectPersister<Insight> mockInsightPersister;
    private ObjectPersister<Customer> mockCustomerPersister;

    @BeforeMethod
    public void setup()
    {
        agenda = new Agenda();
        agenda.setAgendaInsight(TestUtil.createAgendaInsight("", ""));

        retryAgendaRequest = new RetryAgendaRequest();
        retryAgendaRequest.setAgendaId("");

        mockRequestProcessorFactory = mock(RequestProcessorFactory.class);

        mockAgendaRequestProcessor = mock(AgendaRequestProcessor.class);
        mockAgendaProgressRequestProcessor = mock(AgendaProgressRequestProcessor.class);
        mockReadyAgendaPersister = mock(ObjectPersister.class);
        mockInsightPersister = mock(ObjectPersister.class);
        mockCustomerPersister = mock(ObjectPersister.class);

        doReturn(mockAgendaRequestProcessor).when(mockRequestProcessorFactory).createAgendaRequestProcessor(any(), any(), any(), any(), any(), any());
        doReturn(mockAgendaProgressRequestProcessor).when(mockRequestProcessorFactory).createAgendaProgressRequestProcessor(any(), any(), any());

        requestProcessor = new RetryAgendaServiceRequestProcessor(mock(ObjectPersister.class), mock(ObjectPersister.class), mock(ObjectPersister.class),
            mockReadyAgendaPersister, mockInsightPersister, mockCustomerPersister);
        requestProcessor.setRequestProcessorFactory(mockRequestProcessorFactory);
    }

    @Test
    public void testCompleteReset() throws Throwable
    {
        retryAgendaRequest.setParams(Collections.singletonList(RetryAgendaParameter.RESET_ALL.getParameterName()));

        doReturn(TestUtil.createDataObjectResponse(agenda)).when(mockAgendaRequestProcessor).handleGET(any());

        AgendaProgress agendaProgress = TestUtil.createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name());
        agendaProgress.setOperationProgress(new OperationProgress[] {
            TestUtil.createOperationProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name()),
            TestUtil.createOperationProgress(ProcessingState.WAITING, null)
        });

        doReturn(TestUtil.createDataObjectResponse(agendaProgress)).when(mockAgendaProgressRequestProcessor).handleGET(any());

        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaProgressRequestProcessor).handlePUT(any());

        requestProcessor.processPOST(new DefaultServiceRequest<>(retryAgendaRequest));
        verify(mockAgendaProgressRequestProcessor, times(1)).handlePUT(any());
        verify(mockReadyAgendaPersister, times(1)).persist(any());

        // AgendaProgress checks
        Assert.assertEquals(agendaProgress.getProcessingState(), ProcessingState.WAITING);
        Assert.assertEquals(agendaProgress.getProcessingStateMessage(), ProgressResetProcessor.DEFAULT_RESET_STATE_MESSAGE);

        // OperationProgress checks
        for (OperationProgress operationProgress : agendaProgress.getOperationProgress())
        {
            Assert.assertEquals(operationProgress.getProcessingState(), ProcessingState.WAITING);
            Assert.assertEquals(operationProgress.getProcessingStateMessage(), ProgressResetProcessor.DEFAULT_RESET_STATE_MESSAGE);
        }
    }

    @Test
    public void testErrorOnAgendaLookup()
    {
        doReturn(TestUtil.createErrorDataObjecResponse(ERROR_MESSAGE)).when(mockAgendaRequestProcessor).handleGET(any());
        testErrorExecute(1, 0, 0, 0);
    }

    @Test
    public void testNoAgendaFoundOnLookup()
    {
        doReturn(TestUtil.createDataObjectResponse()).when(mockAgendaRequestProcessor).handleGET(any());
        RetryAgendaResponse retryAgendaResponse = testExecute(1, 0, 0, 0);
        Assert.assertNotNull(retryAgendaResponse);
        Assert.assertTrue(retryAgendaResponse.isError());
        Assert.assertTrue(StringUtils.containsIgnoreCase(retryAgendaResponse.getErrorResponse().getDescription(), "not found"));
    }

    @Test
    public void testErrorOnAgendaProgressLookup()
    {
        doReturn(TestUtil.createDataObjectResponse(agenda)).when(mockAgendaRequestProcessor).handleGET(any());
        doReturn(TestUtil.createErrorDataObjecResponse(ERROR_MESSAGE)).when(mockAgendaProgressRequestProcessor).handleGET(any());
        testErrorExecute(1, 1, 0, 0);
    }

    @Test
    public void testNoAgendaProgressFoundOnLookup()
    {
        doReturn(TestUtil.createDataObjectResponse(agenda)).when(mockAgendaRequestProcessor).handleGET(any());
        doReturn(TestUtil.createDataObjectResponse()).when(mockAgendaProgressRequestProcessor).handleGET(any());
        RetryAgendaResponse retryAgendaResponse = testExecute(1, 1, 0, 0);
        Assert.assertNotNull(retryAgendaResponse);
        Assert.assertTrue(retryAgendaResponse.isError());
        Assert.assertTrue(StringUtils.containsIgnoreCase(retryAgendaResponse.getErrorResponse().getDescription(), "not found"));
    }

    @Test
    public void testErrorOnAgendaProgressUpdate()
    {
        doReturn(TestUtil.createDataObjectResponse(agenda)).when(mockAgendaRequestProcessor).handleGET(any());
        doReturn(TestUtil.createDataObjectResponse(new AgendaProgress())).when(mockAgendaProgressRequestProcessor).handleGET(any());
        doReturn(TestUtil.createErrorDataObjecResponse(ERROR_MESSAGE)).when(mockAgendaProgressRequestProcessor).handlePUT(any());
        testErrorExecute(1, 1, 1, 0);
    }

    @Test
    public void testErrorOnReadyAgendaCreate() throws PersistenceException
    {
        AgendaProgress agendaProgress = TestUtil.createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name());
        agendaProgress.setOperationProgress(new OperationProgress[] { TestUtil.createOperationProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name())});

        doReturn(TestUtil.createDataObjectResponse(agenda)).when(mockAgendaRequestProcessor).handleGET(any());
        doReturn(TestUtil.createDataObjectResponse(agendaProgress)).when(mockAgendaProgressRequestProcessor).handleGET(any());
        doReturn(TestUtil.createDataObjectResponse(agendaProgress)).when(mockAgendaProgressRequestProcessor).handlePUT(any());
        doThrow(new PersistenceException(ERROR_MESSAGE)).when(mockReadyAgendaPersister).persist(any());
        testErrorExecute(1, 1, 1, 1);
    }

    @Test
    public void testSkipExecution()
    {
        retryAgendaRequest.setParams(Collections.singletonList(RetryAgendaParameter.SKIP_EXECUTION.getParameterName()));

        AgendaProgress agendaProgress = TestUtil.createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name());
        agendaProgress.setOperationProgress(new OperationProgress[] { TestUtil.createOperationProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name())});

        doReturn(TestUtil.createDataObjectResponse(agenda)).when(mockAgendaRequestProcessor).handleGET(any());
        doReturn(TestUtil.createDataObjectResponse(agendaProgress)).when(mockAgendaProgressRequestProcessor).handleGET(any());
        doReturn(TestUtil.createDataObjectResponse(agendaProgress)).when(mockAgendaProgressRequestProcessor).handlePUT(any());

        testExecute(1, 1, 1, 0);
    }

    @Test
    public void testSkipExecutionOnAgenda()
    {
        AgendaProgress agendaProgress = TestUtil.createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name());
        agendaProgress.setOperationProgress(new OperationProgress[] { TestUtil.createOperationProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name())});

        ParamsMap paramsMap = new ParamsMap();
        paramsMap.put(GeneralParamKey.doNotRun, null);
        agenda.setParams(paramsMap);

        doReturn(TestUtil.createDataObjectResponse(agenda)).when(mockAgendaRequestProcessor).handleGET(any());
        doReturn(TestUtil.createDataObjectResponse(agendaProgress)).when(mockAgendaProgressRequestProcessor).handleGET(any());
        doReturn(TestUtil.createDataObjectResponse(agendaProgress)).when(mockAgendaProgressRequestProcessor).handlePUT(any());

        testExecute(1, 1, 1, 0);
    }

    private void testErrorExecute(int expectedAgendaGets, int expectedAgendaProgressGets, int expectedAgendaProgressPuts,
        int expectedReadyAgendaPersists)
    {
        RetryAgendaResponse response = testExecute(expectedAgendaGets, expectedAgendaProgressGets, expectedAgendaProgressPuts, expectedReadyAgendaPersists);
        Assert.assertNotNull(response);
        Assert.assertTrue(response.isError());
    }

    private RetryAgendaResponse testExecute(int expectedAgendaGets, int expectedAgendaProgressGets, int expectedAgendaProgressPuts,
        int expectedReadyAgendaPersists)
    {
        try
        {
            RetryAgendaResponse response = requestProcessor.processPOST(new DefaultServiceRequest<>(retryAgendaRequest));
            verify(mockAgendaRequestProcessor, times(expectedAgendaGets)).handleGET(any());
            verify(mockAgendaProgressRequestProcessor, times(expectedAgendaProgressGets)).handleGET(any());
            verify(mockAgendaProgressRequestProcessor, times(expectedAgendaProgressPuts)).handlePUT(any());
            verify(mockReadyAgendaPersister, times(expectedReadyAgendaPersists)).persist(any());
            return response;
        }
        catch(Throwable t)
        {
            Assert.fail("Test failed for an unknown reason.", t);
        }
        return null;
    }
}
