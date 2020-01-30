package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.TestUtil;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.factory.RequestProcessorFactory;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.DefaultServiceRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaParameter;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
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
    private OperationProgressRequestProcessor mockOperationProgressRequestProcessor;
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
        mockOperationProgressRequestProcessor = mock(OperationProgressRequestProcessor.class);
        mockReadyAgendaPersister = mock(ObjectPersister.class);
        mockInsightPersister = mock(ObjectPersister.class);
        mockCustomerPersister = mock(ObjectPersister.class);

        doReturn(mockAgendaRequestProcessor).when(mockRequestProcessorFactory).createAgendaRequestProcessor(any(), any(), any(), any(), any(), any());
        doReturn(mockAgendaProgressRequestProcessor).when(mockRequestProcessorFactory).createAgendaProgressRequestProcessor(any(), any(), any());
        doReturn(mockOperationProgressRequestProcessor).when(mockRequestProcessorFactory).createOperationProgressRequestProcessor(any());

        requestProcessor = new RetryAgendaServiceRequestProcessor(mock(ObjectPersister.class), mock(ObjectPersister.class), mock(ObjectPersister.class),
            mockReadyAgendaPersister, mockInsightPersister, mockCustomerPersister, mockRequestProcessorFactory);
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
        int expectedOperationProgressCalls = agendaProgress.getOperationProgress().length;

        doReturn(TestUtil.createDataObjectResponse(agendaProgress)).when(mockAgendaProgressRequestProcessor).handleGET(any());

        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaProgressRequestProcessor).handlePUT(any());
        doReturn(new DefaultDataObjectResponse<>()).when(mockOperationProgressRequestProcessor).handlePUT(any());

        requestProcessor.processPOST(new DefaultServiceRequest<>(retryAgendaRequest));
        verify(mockAgendaProgressRequestProcessor, times(1)).handlePUT(any());
        verify(mockOperationProgressRequestProcessor, times(expectedOperationProgressCalls)).handlePUT(any());
        verify(mockReadyAgendaPersister, times(1)).persist(any());

        // AgendaProgress checks
        Assert.assertEquals(agendaProgress.getProcessingState(), ProcessingState.WAITING);
        Assert.assertNull(agendaProgress.getProcessingStateMessage());

        // OperationProgress checks
        for (OperationProgress operationProgress : agendaProgress.getOperationProgress())
        {
            Assert.assertEquals(operationProgress.getProcessingState(), ProcessingState.WAITING);
            Assert.assertNull(operationProgress.getProcessingStateMessage());
        }
    }

    @Test
    public void testErrorOnAgendaLookup()
    {
        doReturn(TestUtil.createErrorDataObjecResponse(ERROR_MESSAGE)).when(mockAgendaRequestProcessor).handleGET(any());
        testErrorExecute(1, 0, 0, 0, 0);
    }

    @Test
    public void testErrorOnAgendaProgressLookup()
    {
        doReturn(TestUtil.createDataObjectResponse(agenda)).when(mockAgendaRequestProcessor).handleGET(any());
        doReturn(TestUtil.createErrorDataObjecResponse(ERROR_MESSAGE)).when(mockAgendaProgressRequestProcessor).handleGET(any());
        testErrorExecute(1, 1, 0, 0, 0);
    }

    @Test
    public void testErrorOnAgendaProgressUpdate()
    {
        doReturn(TestUtil.createDataObjectResponse(agenda)).when(mockAgendaRequestProcessor).handleGET(any());
        doReturn(TestUtil.createDataObjectResponse(new AgendaProgress())).when(mockAgendaProgressRequestProcessor).handleGET(any());
        doReturn(TestUtil.createErrorDataObjecResponse(ERROR_MESSAGE)).when(mockAgendaProgressRequestProcessor).handlePUT(any());
        testErrorExecute(1, 1, 1, 0, 0);
    }

    @Test
    public void testErrorOnOperationProgressUpdate()
    {
        AgendaProgress agendaProgress = TestUtil.createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name());
        agendaProgress.setOperationProgress(new OperationProgress[] { TestUtil.createOperationProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name())});

        doReturn(TestUtil.createDataObjectResponse(agenda)).when(mockAgendaRequestProcessor).handleGET(any());
        doReturn(TestUtil.createDataObjectResponse(agendaProgress)).when(mockAgendaProgressRequestProcessor).handleGET(any());
        doReturn(TestUtil.createDataObjectResponse(agendaProgress)).when(mockAgendaProgressRequestProcessor).handlePUT(any());
        doReturn(TestUtil.createErrorDataObjecResponse(ERROR_MESSAGE)).when(mockOperationProgressRequestProcessor).handlePUT(any());
        testErrorExecute(1, 1, 1, 1, 0);
    }

    @Test
    public void testErrorOnReadyAgendaCreate() throws PersistenceException
    {
        AgendaProgress agendaProgress = TestUtil.createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name());
        agendaProgress.setOperationProgress(new OperationProgress[] { TestUtil.createOperationProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name())});

        doReturn(TestUtil.createDataObjectResponse(agenda)).when(mockAgendaRequestProcessor).handleGET(any());
        doReturn(TestUtil.createDataObjectResponse(agendaProgress)).when(mockAgendaProgressRequestProcessor).handleGET(any());
        doReturn(TestUtil.createDataObjectResponse(agendaProgress)).when(mockAgendaProgressRequestProcessor).handlePUT(any());
        doReturn(TestUtil.createDataObjectResponse(new OperationProgress())).when(mockOperationProgressRequestProcessor).handlePUT(any());
        doThrow(new PersistenceException(ERROR_MESSAGE)).when(mockReadyAgendaPersister).persist(any());
        testErrorExecute(1, 1, 1, 1, 1);
    }

    private void testErrorExecute(int expectedAgendaGets, int expectedAgendaProgressGets, int expectedAgendaProgressPuts, int expectedOperationProgressPuts,
        int expectedReadyAgendaPersists)
    {
        try
        {
            RetryAgendaResponse response = requestProcessor.processPOST(new DefaultServiceRequest<>(retryAgendaRequest));
            Assert.assertTrue(response.isError());
            verify(mockAgendaRequestProcessor, times(expectedAgendaGets)).handleGET(any());
            verify(mockAgendaProgressRequestProcessor, times(expectedAgendaProgressGets)).handleGET(any());
            verify(mockAgendaProgressRequestProcessor, times(expectedAgendaProgressPuts)).handlePUT(any());
            verify(mockOperationProgressRequestProcessor, times(expectedOperationProgressPuts)).handlePUT(any());
            verify(mockReadyAgendaPersister, times(expectedReadyAgendaPersists)).persist(any());
        }
        catch(Throwable t)
        {
            Assert.fail("Test failed for an unknown reason.", t);
        }
    }
}
