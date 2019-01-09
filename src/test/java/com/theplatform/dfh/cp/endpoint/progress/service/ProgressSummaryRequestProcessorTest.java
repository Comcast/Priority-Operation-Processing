package com.theplatform.dfh.cp.endpoint.progress.service;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.progress.service.api.ProgressSummaryResponse;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.cp.endpoint.progress.service.api.ProgressSummaryRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ProgressSummaryRequestProcessorTest
{
    private ObjectClient<AgendaProgress> mockAgendaProgressClient;
    private ProgressSummaryRequestProcessor progressSummaryRequestProcessor;

    @BeforeMethod
    public void setup()
    {
        progressSummaryRequestProcessor = new ProgressSummaryRequestProcessor();
        mockAgendaProgressClient = (ObjectClient<AgendaProgress>)mock(ObjectClient.class);
        progressSummaryRequestProcessor.setAgendaProgressClient(mockAgendaProgressClient);
    }

    @Test
    public void checkStateCount()
    {
        Assert.assertEquals(ProcessingState.values().length, 3, "Number of processing states changed. This may affect the overall state determination.");
    }

    @DataProvider
    public Object[][] stateProvider()
    {
        return new Object[][]
            {
                {ProcessingState.COMPLETE, new ProcessingState[]{ProcessingState.COMPLETE}},
                {ProcessingState.WAITING, new ProcessingState[]{ProcessingState.WAITING}},
                {ProcessingState.EXECUTING, new ProcessingState[]{ProcessingState.EXECUTING}},
                {ProcessingState.WAITING, new ProcessingState[]{ProcessingState.WAITING, ProcessingState.WAITING}},
                {ProcessingState.EXECUTING, new ProcessingState[]{ProcessingState.COMPLETE, ProcessingState.WAITING}},
                {ProcessingState.COMPLETE, new ProcessingState[]{ProcessingState.COMPLETE, ProcessingState.COMPLETE}},
            };
    }

    @Test(dataProvider = "stateProvider")
    public void testGetProgressSummary(ProcessingState expectedState, ProcessingState[] states) throws Exception
    {
        setupAgendaProgress(states);
        ProgressSummaryResponse progressSummaryResponse = progressSummaryRequestProcessor.getProgressSummary(new ProgressSummaryRequest().setLinkId("theLinkId"));
        Assert.assertEquals(progressSummaryResponse.getProcessingState(), expectedState);
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void testMissingLinkId() throws Exception
    {
        progressSummaryRequestProcessor.getProgressSummary(new ProgressSummaryRequest());
    }

    private void setupAgendaProgress(ProcessingState[] states) throws Exception
    {
        DataObjectResponse<AgendaProgress> dataObjectFeed = new DefaultDataObjectResponse<>();
        dataObjectFeed.addAll(Arrays.stream(states)
            .map(processingState ->
            {
                AgendaProgress agendaProgress = new AgendaProgress();
                agendaProgress.setProcessingState(processingState);
                return agendaProgress;
            }).collect(Collectors.toList())
        );
        doReturn(dataObjectFeed).when(mockAgendaProgressClient).getObjects(anyList());
    }
}
