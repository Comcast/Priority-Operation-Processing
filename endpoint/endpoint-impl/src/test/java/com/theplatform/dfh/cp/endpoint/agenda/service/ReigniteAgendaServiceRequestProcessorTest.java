package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.params.GeneralParamKey;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.CompleteStateMessage;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.TestUtil;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.agenda.service.reset.ProgressResetProcessor;
import com.theplatform.dfh.cp.endpoint.agenda.service.reset.ProgressResetResult;
import com.theplatform.dfh.cp.endpoint.factory.RequestProcessorFactory;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.DefaultServiceRequest;
import com.comcast.pop.endpoint.api.agenda.ReigniteAgendaParameter;
import com.comcast.pop.endpoint.api.agenda.ReigniteAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.ReigniteAgendaResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReigniteAgendaServiceRequestProcessorTest
{
    final String ERROR_MESSAGE = "theError";

    private ReigniteAgendaServiceRequestProcessor requestProcessor;

    private ReigniteAgendaRequest reigniteAgendaRequest;
    private Agenda agenda;

    private RequestProcessorFactory mockRequestProcessorFactory;
    private AgendaRequestProcessor mockAgendaRequestProcessor;
    private AgendaProgressRequestProcessor mockAgendaProgressRequestProcessor;
    private ObjectPersister<ReadyAgenda> mockReadyAgendaPersister;
    private ObjectPersister<Insight> mockInsightPersister;
    private ObjectPersister<Customer> mockCustomerPersister;
    private ProgressResetProcessor mockProgressResetProcessor;
    private ProgressResetResult progressResetResult;

    @BeforeMethod
    public void setup()
    {
        agenda = new Agenda();
        agenda.setAgendaInsight(TestUtil.createAgendaInsight("", ""));

        reigniteAgendaRequest = new ReigniteAgendaRequest();
        reigniteAgendaRequest.setAgendaId("");

        mockRequestProcessorFactory = mock(RequestProcessorFactory.class);

        progressResetResult = new ProgressResetResult();
        mockProgressResetProcessor = mock(ProgressResetProcessor.class);
        doReturn(progressResetResult).when(mockProgressResetProcessor).resetProgress(any(), any(), any());

        mockAgendaRequestProcessor = mock(AgendaRequestProcessor.class);
        mockAgendaProgressRequestProcessor = mock(AgendaProgressRequestProcessor.class);
        mockReadyAgendaPersister = mock(ObjectPersister.class);
        mockInsightPersister = mock(ObjectPersister.class);
        mockCustomerPersister = mock(ObjectPersister.class);

        doReturn(mockAgendaRequestProcessor).when(mockRequestProcessorFactory).createAgendaRequestProcessor(any(), any(), any(), any(), any(), any());
        doReturn(mockAgendaProgressRequestProcessor).when(mockRequestProcessorFactory).createAgendaProgressRequestProcessor(any(), any(), any());

        requestProcessor = new ReigniteAgendaServiceRequestProcessor(mock(ObjectPersister.class), mock(ObjectPersister.class), mock(ObjectPersister.class),
            mockReadyAgendaPersister, mockInsightPersister, mockCustomerPersister);
        requestProcessor.setProgressResetProcessor(mockProgressResetProcessor);
        requestProcessor.setRequestProcessorFactory(mockRequestProcessorFactory);
    }

    @Test
    public void testCompleteReset() throws Throwable
    {
        reigniteAgendaRequest.setParams(Collections.singletonList(ReigniteAgendaParameter.RESET_ALL.getParameterName()));

        doReturn(TestUtil.createDataObjectResponse(agenda)).when(mockAgendaRequestProcessor).handleGET(any());

        progressResetResult.setOperationsToReset(new HashSet<>());
        progressResetResult.setOperationsToDelete(new HashSet<>());

        AgendaProgress agendaProgress = TestUtil.createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name());
        agendaProgress.setOperationProgress(new OperationProgress[] {
            TestUtil.createOperationProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.name()),
            TestUtil.createOperationProgress(ProcessingState.WAITING, null)
        });

        doReturn(TestUtil.createDataObjectResponse(agendaProgress)).when(mockAgendaProgressRequestProcessor).handleGET(any());

        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaProgressRequestProcessor).handlePUT(any());

        requestProcessor.processPOST(new DefaultServiceRequest<>(reigniteAgendaRequest));
        verify(mockAgendaProgressRequestProcessor, times(1)).handlePUT(any());
        verify(mockReadyAgendaPersister, times(1)).persist(any());
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
        ReigniteAgendaResponse reigniteAgendaResponse = testExecute(1, 0, 0, 0);
        Assert.assertNotNull(reigniteAgendaResponse);
        Assert.assertTrue(reigniteAgendaResponse.isError());
        Assert.assertTrue(StringUtils.containsIgnoreCase(reigniteAgendaResponse.getErrorResponse().getDescription(), "not found"));
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
        ReigniteAgendaResponse reigniteAgendaResponse = testExecute(1, 1, 0, 0);
        Assert.assertNotNull(reigniteAgendaResponse);
        Assert.assertTrue(reigniteAgendaResponse.isError());
        Assert.assertTrue(StringUtils.containsIgnoreCase(reigniteAgendaResponse.getErrorResponse().getDescription(), "not found"));
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
        reigniteAgendaRequest.setParams(Collections.singletonList(ReigniteAgendaParameter.SKIP_EXECUTION.getParameterName()));

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
        ReigniteAgendaResponse response = testExecute(expectedAgendaGets, expectedAgendaProgressGets, expectedAgendaProgressPuts, expectedReadyAgendaPersists);
        Assert.assertNotNull(response);
        Assert.assertTrue(response.isError());
    }

    private ReigniteAgendaResponse testExecute(int expectedAgendaGets, int expectedAgendaProgressGets, int expectedAgendaProgressPuts,
        int expectedReadyAgendaPersists)
    {
        try
        {
            ReigniteAgendaResponse response = requestProcessor.processPOST(new DefaultServiceRequest<>(reigniteAgendaRequest));
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
