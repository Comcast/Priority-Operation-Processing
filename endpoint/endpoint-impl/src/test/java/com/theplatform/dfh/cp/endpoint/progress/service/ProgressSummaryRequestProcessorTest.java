package com.theplatform.dfh.cp.endpoint.progress.service;

import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comcast.pop.endpoint.base.visibility.VisibilityFilter;
import com.comcast.pop.endpoint.api.progress.ProgressSummaryResponse;
import com.comcast.pop.endpoint.api.progress.ProgressSummaryRequest;
import com.comcast.pop.endpoint.api.DefaultServiceRequest;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ProgressSummaryRequestProcessorTest
{
    private ObjectClient<AgendaProgress> mockAgendaProgressClient;
    private ProgressSummaryRequestProcessor progressSummaryRequestProcessor;
    private VisibilityFilter mockVisibilityFilter;

    @BeforeMethod
    public void setup()
    {
        mockAgendaProgressClient = (ObjectClient<AgendaProgress>)mock(ObjectClient.class);
        mockVisibilityFilter = mock(VisibilityFilter.class);
        progressSummaryRequestProcessor = new ProgressSummaryRequestProcessor();
        progressSummaryRequestProcessor.setAgendaProgressClient(mockAgendaProgressClient);
        progressSummaryRequestProcessor.setVisibilityFilter(mockVisibilityFilter);
        // by default the filterByVisible just returns the input list
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return invocationOnMock.getArguments()[1];
            }
        }).when(mockVisibilityFilter).filterByVisible(any(), any());
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
    public void testGetProgressSummary(ProcessingState expectedState, ProcessingState[] states)
    {
        setupAgendaProgress(states);
        ServiceRequest<ProgressSummaryRequest> request = new DefaultServiceRequest<>(new ProgressSummaryRequest().setLinkId("theLinkId"));
        ProgressSummaryResponse progressSummaryResponse = progressSummaryRequestProcessor.handlePOST(request);
        Assert.assertEquals(progressSummaryResponse.getProcessingState(), expectedState);
    }

    @Test
    public void testGetProgressNotVisible()
    {
        setupAgendaProgress(new ProcessingState[] { ProcessingState.COMPLETE, ProcessingState.COMPLETE } );
        // everything is filtered
        doReturn(new ArrayList<>()).when(mockVisibilityFilter).filterByVisible(any(), any());
        ProgressSummaryResponse progressSummaryResponse = progressSummaryRequestProcessor.handlePOST(
            new DefaultServiceRequest<>(new ProgressSummaryRequest().setLinkId("theLinkId"))
        );
        Assert.assertEquals(progressSummaryResponse.getProgressList().size(), 0);
    }

    @DataProvider
    public Object[][] percentCompleteProvider()
    {
        return new Object[][]
            {
                {null, 0},
                {createProgressListWithPercents((Double)null), 0},
                {createProgressListWithPercents(null, 45d), 22.5d},
                {createProgressListWithPercents(45d, null), 22.5d},
                {createProgressListWithPercents(150d, 150d, 150d), 100},
            };
    }

    @Test(dataProvider = "percentCompleteProvider")
    public void testGetPercentComplete(List<AgendaProgress> progressList, final double EXPECTED_VALUE)
    {
        Assert.assertEquals(progressSummaryRequestProcessor.getPercentComplete(progressList), EXPECTED_VALUE);
    }

    private List<AgendaProgress> createProgressListWithPercents(Double... percents)
    {
        return Arrays.stream(percents).map(percentComplete ->
            {
               AgendaProgress agendaProgress = new AgendaProgress();
               agendaProgress.setPercentComplete(percentComplete);
               return agendaProgress;
            }
        ).collect(Collectors.toList());
    }

    private void setupAgendaProgress(ProcessingState[] states)
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
