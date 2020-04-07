package com.cts.fission.callback.progress.retry;

import com.comcast.fission.endpoint.api.ErrorResponseFactory;
import com.comcast.fission.endpoint.api.agenda.ReigniteAgendaResponse;
import com.comcast.fission.endpoint.api.data.DataObjectResponse;
import com.comcast.fission.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.client.AgendaServiceClient;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.object.api.IdentifiedObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AgendaProgressProcessorTest
{
    private AgendaProgressProcessor processor;
    private AgendaServiceClient mockAgendaServiceClient;
    private ObjectClient<AgendaProgress> mockAgendaProgressClient;

    @BeforeMethod
    public void setup()
    {
        mockAgendaProgressClient = mock(ObjectClient.class);
        mockAgendaServiceClient = mock(AgendaServiceClient.class);
        processor = new AgendaProgressProcessor(mockAgendaServiceClient, mockAgendaProgressClient);
    }

    @DataProvider
    public Object[][] ignoredProgressStateProvider()
    {
        return new Object[][]
            {
                {null},
                {createAgendaProgress(null, null, null, null)},
                {createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.SUCCEEDED.toString(), null, null)},
                {createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), 1, 1)},
                {createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), null, 1)}, // attempts defaults to 1
                {createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), 3, null)}, // max attempts defaults to 3
            };
    }

    @Test(dataProvider = "ignoredProgressStateProvider")
    public void testIgnoredAgendaProgressState(AgendaProgress agendaProgress)
    {
        doReturn(createDataObjectResponse(agendaProgress)).when(mockAgendaProgressClient).getObject(any());
        processor.process(agendaProgress);
        verify(mockAgendaServiceClient, times(0)).reigniteAgenda(any());
    }

    @DataProvider
    public Object[][] reignitedProgressStateProvider()
    {
        return new Object[][]
            {
                {createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), null, 2)}, // attempts defaults to 1
                {createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), 0, 1)},
                {createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), 2, null)}, // max attempts defaults to 3
            };
    }

    @Test(dataProvider = "reignitedProgressStateProvider")
    public void testReignitedAgendaProgressState(AgendaProgress agendaProgress)
    {
        doReturn(createDataObjectResponse(agendaProgress)).when(mockAgendaProgressClient).getObject(any());
        doReturn(new ReigniteAgendaResponse()).when(mockAgendaServiceClient).reigniteAgenda(any());
        processor.process(agendaProgress);
        verify(mockAgendaServiceClient, times(1)).reigniteAgenda(any());
    }

    @Test
    public void testErrorRetrievingAgendaProgress()
    {
        AgendaProgress agendaProgress = createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), 0, 3);
        DataObjectResponse<AgendaProgress> response = new DefaultDataObjectResponse<>();
        response.setErrorResponse(ErrorResponseFactory.runtimeServiceException("", ""));
        doReturn(response).when(mockAgendaProgressClient).getObject(any());
        processor.process(agendaProgress);
        // basically just check that nothing was done
        verify(mockAgendaServiceClient, times(0)).reigniteAgenda(any());
    }

    @Test
    public void testMissingAgendaProgress()
    {
        AgendaProgress agendaProgress = createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), 0, 3);
        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaProgressClient).getObject(any());
        processor.process(agendaProgress);
        // basically just check that nothing was done
        verify(mockAgendaServiceClient, times(0)).reigniteAgenda(any());
    }

    @Test
    public void testReigniteCallError()
    {
        AgendaProgress agendaProgress = createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), 0, 3);
        ReigniteAgendaResponse reigniteAgendaResponse = new ReigniteAgendaResponse();
        reigniteAgendaResponse.setErrorResponse(ErrorResponseFactory.runtimeServiceException("", ""));
        doReturn(createDataObjectResponse(agendaProgress)).when(mockAgendaProgressClient).getObject(any());
        doReturn(reigniteAgendaResponse).when(mockAgendaServiceClient).reigniteAgenda(any());
        processor.process(agendaProgress);
        // basically just check that nothing was done
        verify(mockAgendaServiceClient, times(1)).reigniteAgenda(any());
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Failed to call reignite for Agenda.*")
    public void testReigniteException()
    {
        AgendaProgress agendaProgress = createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), 0, 3);
        doReturn(createDataObjectResponse(agendaProgress)).when(mockAgendaProgressClient).getObject(any());
        doThrow(new RuntimeException("ReigniteException")).when(mockAgendaServiceClient).reigniteAgenda(any());
        processor.process(agendaProgress);
        // basically just check that nothing was done
        verify(mockAgendaServiceClient, times(1)).reigniteAgenda(any());
    }

    private <T extends IdentifiedObject> DataObjectResponse<T> createDataObjectResponse(T object)
    {
        DataObjectResponse<T> response = new DefaultDataObjectResponse<>();
        response.add(object);
        return response;
    }

    public AgendaProgress createAgendaProgress(ProcessingState processingState, String message, Integer attemptsCompleted, Integer maxAttempts)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setProcessingState(processingState);
        agendaProgress.setProcessingStateMessage(message);
        agendaProgress.setAttemptsCompleted(attemptsCompleted);
        agendaProgress.setMaximumAttempts(maxAttempts);
        return agendaProgress;
    }
}
