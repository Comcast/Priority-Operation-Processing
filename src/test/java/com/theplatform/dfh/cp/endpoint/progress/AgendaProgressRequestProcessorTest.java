package com.theplatform.dfh.cp.endpoint.progress;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.base.visibility.NoOpVisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityMethod;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.ObjectNotFoundException;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.query.ByFields;
import com.theplatform.dfh.endpoint.api.data.query.ById;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.query.Query;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AgendaProgressRequestProcessorTest
{
    final String AGENDA_PROGRESS_ID = UUID.randomUUID().toString();
    private static final String FIELD_1_NAME = "field1";
    private static final String FIELD_2_NAME = "field2";

    private AgendaProgressRequestProcessor requestProcessor;
    private ObjectPersister<AgendaProgress> mockAgendaProgressPersister;
    private ObjectPersister<Agenda> mockAgendaPersister;
    private OperationProgressRequestProcessor mockOperationProgressRequestProcessor;

    @BeforeMethod
    public void setup()
    {
        mockAgendaProgressPersister = mock(ObjectPersister.class);
        mockAgendaPersister = mock(ObjectPersister.class);
        mockOperationProgressRequestProcessor = mock(OperationProgressRequestProcessor.class);
        requestProcessor = new AgendaProgressRequestProcessor(mockAgendaProgressPersister, mockAgendaPersister, null);
        requestProcessor.setOperationProgressClient(mockOperationProgressRequestProcessor);
    }

    @DataProvider
    public Object[][] shouldReturnFieldProvider()
    {
        return new Object[][]
            {
                {null, FIELD_1_NAME, true},
                { Collections.singletonList(new ByFields(FIELD_1_NAME)), FIELD_1_NAME, true},
                { Collections.singletonList(new ByFields(FIELD_1_NAME)), FIELD_2_NAME, false},
                { Collections.singletonList(new ById(UUID.randomUUID().toString())), FIELD_1_NAME, true},
                { Arrays.asList(new ByFields(FIELD_1_NAME), new ByFields(FIELD_2_NAME)), FIELD_2_NAME, true}
            };
    }

    @Test(dataProvider = "shouldReturnFieldProvider")
    public void testShouldReturnField(List<Query> queryList, String fieldName, final boolean EXPECTED_RESULT)
    {
        Assert.assertEquals(requestProcessor.shouldReturnField(new DefaultDataObjectRequest<>(queryList, null, null), fieldName), EXPECTED_RESULT);
    }

    @DataProvider
    public Object[][] agendaProgressAttemptsProvider()
    {
        return new Object[][]
            {
                {null, null, null},
                {null, createAgendaProgress(ProcessingState.COMPLETE, null), null},
                {createAgendaProgress(ProcessingState.COMPLETE, null), null, null},
                {createAgendaProgress(ProcessingState.COMPLETE, null), createAgendaProgress(ProcessingState.COMPLETE, null), null},
                {createAgendaProgress(ProcessingState.COMPLETE, null), createAgendaProgress(null, null), 1},
                {createAgendaProgress(ProcessingState.COMPLETE, null), createAgendaProgress(null, 2), 3}
            };
    }

    @Test(dataProvider = "agendaProgressAttemptsProvider")
    public void testUpdateAgendaProgressAttemptsOnComplete(AgendaProgress updatedProgress, AgendaProgress currentProgress, Integer expectedAttemptsCompleted)
    {
        requestProcessor.updateAgendaProgressAttemptsOnComplete(updatedProgress, currentProgress);
        if(updatedProgress != null)
            Assert.assertEquals(updatedProgress.getAttemptsCompleted(), expectedAttemptsCompleted);
    }

    @Test
    public void testBasicAgendaProgressPUT() throws PersistenceException
    {
        AgendaProgress agendaProgress = createAgendaProgress(ProcessingState.EXECUTING, 0);
        testHandlePUTSuccess(agendaProgress, false);
    }

    @Test
    public void testAgendaProgressWithOperationProgressPUT() throws PersistenceException
    {
        final String OP_NAME = "Op.1";
        AgendaProgress agendaProgress = createAgendaProgress(ProcessingState.EXECUTING, 0);
        agendaProgress.setOperationProgress(new OperationProgress[]{ createOperationProgress(OP_NAME, ProcessingState.EXECUTING) });
        doReturn(new DefaultDataObjectResponse<>()).when(mockOperationProgressRequestProcessor).handlePUT(any());
        testHandlePUTSuccess(agendaProgress, false);
        verify(mockOperationProgressRequestProcessor, times(1)).handlePUT(any());
    }

    @Test
    public void testAgendaProgressWithNewOperationProgressPUT() throws PersistenceException
    {
        final String OP_NAME = "Op.1";
        AgendaProgress agendaProgress = createAgendaProgress(ProcessingState.EXECUTING, 0);
        agendaProgress.setOperationProgress(new OperationProgress[]{ createOperationProgress(OP_NAME, ProcessingState.EXECUTING) });
        DataObjectResponse<OperationProgress> failedPUTResponse = new DefaultDataObjectResponse<>();
        failedPUTResponse.setErrorResponse(ErrorResponseFactory.objectNotFound(new ObjectNotFoundException(), ""));
        doReturn(failedPUTResponse).when(mockOperationProgressRequestProcessor).handlePUT(any());
        doReturn(new DefaultDataObjectResponse<>()).when(mockOperationProgressRequestProcessor).handlePOST(any());
        testHandlePUTSuccess(agendaProgress, false);
        verify(mockOperationProgressRequestProcessor, times(1)).handlePUT(any());
        verify(mockOperationProgressRequestProcessor, times(1)).handlePOST(any());
    }

    @Test
    public void testAgendaProgressWithNewOperationProgressPUTFail() throws PersistenceException
    {
        final String OP_NAME = "Op.1";
        AgendaProgress agendaProgress = createAgendaProgress(ProcessingState.EXECUTING, 0);
        agendaProgress.setOperationProgress(new OperationProgress[]{ createOperationProgress(OP_NAME, ProcessingState.EXECUTING) });
        DataObjectResponse<OperationProgress> failedPUTResponse = new DefaultDataObjectResponse<>();
        failedPUTResponse.setErrorResponse(ErrorResponseFactory.objectNotFound(new ObjectNotFoundException(), ""));
        doReturn(failedPUTResponse).when(mockOperationProgressRequestProcessor).handlePUT(any());
        doReturn(failedPUTResponse).when(mockOperationProgressRequestProcessor).handlePOST(any());
        testHandlePUTSuccess(agendaProgress, true);
        verify(mockOperationProgressRequestProcessor, times(1)).handlePUT(any());
        verify(mockOperationProgressRequestProcessor, times(1)).handlePOST(any());
    }

    private void testHandlePUTSuccess(AgendaProgress agendaProgress, boolean expectError) throws PersistenceException
    {
        DefaultDataObjectRequest<AgendaProgress> agendaProgressRequest = new DefaultDataObjectRequest<>();
        agendaProgressRequest.setId(agendaProgress.getId());
        agendaProgressRequest.setDataObject(agendaProgress);

        requestProcessor.setVisibilityFilter(VisibilityMethod.GET, new NoOpVisibilityFilter<>());
        requestProcessor.setVisibilityFilter(VisibilityMethod.PUT, new NoOpVisibilityFilter<>());
        DataObjectFeed<AgendaProgress> progressFeed = new DataObjectFeed<>();
        progressFeed.add(agendaProgress);
        doReturn(progressFeed).when(mockAgendaProgressPersister).retrieve(anyList());
        doReturn(agendaProgress).when(mockAgendaProgressPersister).retrieve(anyString());

        DataObjectResponse<AgendaProgress> progressPUTResponse = requestProcessor.handlePUT(agendaProgressRequest);
        Assert.assertEquals(progressPUTResponse.isError(), expectError);
    }

    private AgendaProgress createAgendaProgress(ProcessingState processingState, Integer attemptsCompleted)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setProcessingState(processingState);
        agendaProgress.setAttemptsCompleted(attemptsCompleted);
        agendaProgress.setId(AGENDA_PROGRESS_ID);
        return agendaProgress;
    }

    private OperationProgress createOperationProgress(String operationName, ProcessingState processingState)
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setProcessingState(processingState);
        operationProgress.setOperation(operationName);
        return operationProgress;
    }
}
