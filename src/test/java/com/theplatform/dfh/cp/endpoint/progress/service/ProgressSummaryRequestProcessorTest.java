package com.theplatform.dfh.cp.endpoint.progress.service;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.client.HttpCPObjectClient;
import com.theplatform.dfh.cp.endpoint.progress.service.api.ProgressSummaryRequest;
import com.theplatform.dfh.cp.endpoint.progress.service.api.ProgressSummaryResult;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ProgressSummaryRequestProcessorTest
{
    private HttpCPObjectClient<AgendaProgress> mockAgendaProgressClient;
    private ProgressSummaryRequestProcessor progressSummaryRequestProcessor;

    @BeforeMethod
    public void setup()
    {
        mockAgendaProgressClient = (HttpCPObjectClient<AgendaProgress>)mock(HttpCPObjectClient.class);
        progressSummaryRequestProcessor = new ProgressSummaryRequestProcessor(mockAgendaProgressClient);
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
        ProgressSummaryResult progressSummaryResult = progressSummaryRequestProcessor.getProgressSummary(new ProgressSummaryRequest().setLinkId("theLinkId"));
        Assert.assertEquals(progressSummaryResult.getProcessingState(), expectedState);
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void testMissingLinkId() throws Exception
    {
        progressSummaryRequestProcessor.getProgressSummary(new ProgressSummaryRequest());
    }

    private void setupAgendaProgress(ProcessingState[] states) throws Exception
    {
        DataObjectFeed<AgendaProgress> dataObjectFeed = new DataObjectFeed<>();
        dataObjectFeed.addAll(Arrays.stream(states)
            .map(processingState ->
            {
                AgendaProgress agendaProgress = new AgendaProgress();
                agendaProgress.setProcessingState(processingState);
                return agendaProgress;
            }).collect(Collectors.toList())
        );
        doReturn(dataObjectFeed).when(mockAgendaProgressClient).getObjects(anyString());
    }
}
