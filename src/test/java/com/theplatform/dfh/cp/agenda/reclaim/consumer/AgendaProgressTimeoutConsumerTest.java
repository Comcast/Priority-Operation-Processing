package com.theplatform.dfh.cp.agenda.reclaim.consumer;

import com.theplatform.com.dfh.modules.sync.util.ConsumerResult;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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
        consumer = spy(new AgendaProgressTimeoutConsumer(mockAgendaProgressClient));
    }

    @Test
    public void testConsumeNull()
    {
        ConsumerResult consumerResult = consumer.consume(null, Instant.now().plusSeconds(60));
        Assert.assertEquals(consumerResult.getItemsConsumedCount(), 0);
        verify(consumer, times(0)).updateAgendaProgress(any());
    }

    @Test
    public void testConsumeNone()
    {
        ConsumerResult consumerResult = consumer.consume(new ArrayList<>(), Instant.now().plusSeconds(60));
        Assert.assertEquals(consumerResult.getItemsConsumedCount(), 0);
        verify(consumer, times(0)).updateAgendaProgress(any());
    }

    @Test
    public void testConsumeTimeout()
    {
        ConsumerResult consumerResult = consumer.consume(Arrays.asList(new AgendaProgress(), new AgendaProgress()), Instant.now().minusSeconds(60));
        Assert.assertEquals(consumerResult.getItemsConsumedCount(), 1);
        verify(consumer, times(1)).updateAgendaProgress(any());
    }

    @Test
    public void testConsume()
    {
        final int ITEMS_TO_CONSUME = 50;
        ConsumerResult consumerResult = consumer.consume(
            IntStream.range(0, ITEMS_TO_CONSUME).mapToObj(i -> new AgendaProgress()).collect(Collectors.toList()),
            Instant.now().plusSeconds(60));
        Assert.assertEquals(consumerResult.getItemsConsumedCount(), ITEMS_TO_CONSUME);
        verify(consumer, times(ITEMS_TO_CONSUME)).updateAgendaProgress(any());
    }

    @Test
    public void testUpdateAgendaProgressException()
    {
        doThrow(new RuntimeException()).when(mockAgendaProgressClient).updateObject(any(), any());
        Assert.assertFalse(consumer.updateAgendaProgress(createTimedOutAgendaProgress()));
        verify(mockAgendaProgressClient, times(1)).updateObject(any(), any());
    }

    @DataProvider Object[][] updateAgendaProgressProvider()
    {
        return new Object[][]
            {
                {createTimedOutAgendaProgress()},
                {createTimedOutAgendaProgress(2)},
            };
    }

    @Test(dataProvider = "updateAgendaProgressProvider")
    public void testUpdateAgendaProgress(AgendaProgress existingProgress)
    {
        final int EXPECTED_DIAGNOSTICS_COUNT = existingProgress.getDiagnosticEvents() == null
            ? 1
            : existingProgress.getDiagnosticEvents().length + 1;

        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                AgendaProgress agendaProgress = (AgendaProgress)invocationOnMock.getArguments()[0];
                Assert.assertEquals(ProcessingState.COMPLETE, agendaProgress.getProcessingState());
                Assert.assertEquals(CompleteStateMessage.FAILED.toString(), agendaProgress.getProcessingStateMessage());
                Assert.assertNotNull(agendaProgress.getDiagnosticEvents());
                Assert.assertEquals(agendaProgress.getDiagnosticEvents().length, EXPECTED_DIAGNOSTICS_COUNT);
                return new DefaultDataObjectResponse<AgendaProgress>();
            }
        }).when(mockAgendaProgressClient).updateObject(any(), any());

        Assert.assertTrue(consumer.updateAgendaProgress(existingProgress));

        verify(mockAgendaProgressClient, times(1)).updateObject(any(), any());
    }

    private AgendaProgress createTimedOutAgendaProgress()
    {
        return createTimedOutAgendaProgress(0);
    }

    private AgendaProgress createTimedOutAgendaProgress(int existingDiagnosticEvents)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setProcessingState(ProcessingState.EXECUTING);
        agendaProgress.setProcessingStateMessage("Executing...");
        agendaProgress.setUpdatedTime(TIMED_OUT_DATE);

        if(existingDiagnosticEvents > 0)
            agendaProgress.setDiagnosticEvents(
                IntStream.range(0, existingDiagnosticEvents)
                    .mapToObj(i -> new DiagnosticEvent())
                    .collect(Collectors.toList())
                    .toArray(new DiagnosticEvent[0]));

        return agendaProgress;
    }
}
