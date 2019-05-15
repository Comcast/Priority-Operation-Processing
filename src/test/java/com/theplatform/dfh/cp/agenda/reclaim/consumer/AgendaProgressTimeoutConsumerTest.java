package com.theplatform.dfh.cp.agenda.reclaim.consumer;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AgendaProgressTimeoutConsumerTest
{
    private final Date TIMED_OUT_DATE = new Date(Instant.now().minusSeconds(65535).toEpochMilli());

    private AgendaProgressTimeoutConsumer consumer;
    private HttpObjectClient<AgendaProgress> mockAgendaProgressClient;

    @BeforeMethod
    public void setup()
    {
        mockAgendaProgressClient = (HttpObjectClient<AgendaProgress>)mock(HttpObjectClient.class);
        consumer = new AgendaProgressTimeoutConsumer(mockAgendaProgressClient);
    }

    @Test
    public void testUpdateAgendaProgressException()
    {
        doThrow(new RuntimeException()).when(mockAgendaProgressClient).updateObject(any(), any());
        Assert.assertFalse(consumer.updateAgendaProgress(createTimedOutAgendaProgress()));
        verify(mockAgendaProgressClient, times(1)).updateObject(any(), any());
    }

    @Test
    public void testUpdateAgendaProgress()
    {
        Assert.assertTrue(consumer.updateAgendaProgress(createTimedOutAgendaProgress()));
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                AgendaProgress agendaProgress = (AgendaProgress)invocationOnMock.getArguments()[0];
                Assert.assertEquals(ProcessingState.COMPLETE, agendaProgress.getProcessingState());
                Assert.assertEquals(CompleteStateMessage.FAILED.toString(), agendaProgress.getProcessingStateMessage());
                Assert.assertNotNull(agendaProgress.getDiagnosticEvents());
                Assert.assertEquals(agendaProgress.getDiagnosticEvents().length, 1);
                return new DefaultDataObjectResponse<AgendaProgress>();
            }
        }).when(mockAgendaProgressClient).updateObject(any(), any());
        verify(mockAgendaProgressClient, times(1)).updateObject(any(), any());
    }

    private AgendaProgress createTimedOutAgendaProgress()
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setProcessingState(ProcessingState.EXECUTING);
        agendaProgress.setProcessingStateMessage("Executing...");
        agendaProgress.setUpdatedTime(TIMED_OUT_DATE);
        return new AgendaProgress();
    }
}
