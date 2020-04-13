package com.comcast.pop.callback.progress.retry;

import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.agenda.RerunAgendaResponse;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.CompleteStateMessage;
import com.comcast.pop.api.progress.ProcessingState;
import com.comcast.pop.endpoint.client.AgendaServiceClient;
import com.comcast.pop.endpoint.client.ObjectClient;
import com.comcast.pop.object.api.IdentifiedObject;
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
        verify(mockAgendaServiceClient, times(0)).rerunAgenda(any());
    }

    @DataProvider
    public Object[][] rerunProgressStateProvider()
    {
        return new Object[][]
            {
                {createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), null, 2)}, // attempts defaults to 1
                {createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), 0, 1)},
                {createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), 2, null)}, // max attempts defaults to 3
            };
    }

    @Test(dataProvider = "rerunProgressStateProvider")
    public void testRerunAgendaProgressState(AgendaProgress agendaProgress)
    {
        doReturn(createDataObjectResponse(agendaProgress)).when(mockAgendaProgressClient).getObject(any());
        doReturn(new RerunAgendaResponse()).when(mockAgendaServiceClient).rerunAgenda(any());
        processor.process(agendaProgress);
        verify(mockAgendaServiceClient, times(1)).rerunAgenda(any());
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
        verify(mockAgendaServiceClient, times(0)).rerunAgenda(any());
    }

    @Test
    public void testMissingAgendaProgress()
    {
        AgendaProgress agendaProgress = createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), 0, 3);
        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaProgressClient).getObject(any());
        processor.process(agendaProgress);
        // basically just check that nothing was done
        verify(mockAgendaServiceClient, times(0)).rerunAgenda(any());
    }

    @Test
    public void testReigniteCallError()
    {
        AgendaProgress agendaProgress = createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), 0, 3);
        RerunAgendaResponse rerunAgendaResponse = new RerunAgendaResponse();
        rerunAgendaResponse.setErrorResponse(ErrorResponseFactory.runtimeServiceException("", ""));
        doReturn(createDataObjectResponse(agendaProgress)).when(mockAgendaProgressClient).getObject(any());
        doReturn(rerunAgendaResponse).when(mockAgendaServiceClient).rerunAgenda(any());
        processor.process(agendaProgress);
        // basically just check that nothing was done
        verify(mockAgendaServiceClient, times(1)).rerunAgenda(any());
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Failed to call reignite for Agenda.*")
    public void testRerunException()
    {
        AgendaProgress agendaProgress = createAgendaProgress(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), 0, 3);
        doReturn(createDataObjectResponse(agendaProgress)).when(mockAgendaProgressClient).getObject(any());
        doThrow(new RuntimeException("RerunException")).when(mockAgendaServiceClient).rerunAgenda(any());
        processor.process(agendaProgress);
        // basically just check that nothing was done
        verify(mockAgendaServiceClient, times(1)).rerunAgenda(any());
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
