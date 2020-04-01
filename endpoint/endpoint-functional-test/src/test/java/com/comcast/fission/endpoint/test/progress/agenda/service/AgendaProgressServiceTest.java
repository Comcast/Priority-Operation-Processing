package com.comcast.fission.endpoint.test.progress.agenda.service;

import com.comcast.fission.endpoint.api.progress.ProgressSummaryRequest;
import com.comcast.fission.endpoint.api.progress.ProgressSummaryResponse;
import com.comcast.fission.endpoint.test.base.EndpointTestBase;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.stream.IntStream;

public class AgendaProgressServiceTest extends EndpointTestBase
{
    private final int AGENDAS_TO_TEST = 3;
    private final String LINK_ID = UUID.randomUUID().toString();

    @Test
    public void testSummaryGet()
    {
        IntStream.range(0, AGENDAS_TO_TEST).forEach(i -> persistTestObject());
        ProgressSummaryResponse progressSummaryResponse = progressServiceClient.getProgressSummary(new ProgressSummaryRequest(LINK_ID));
        Assert.assertNotNull(progressSummaryResponse);
        verifyNoError(progressSummaryResponse);
        Assert.assertEquals(progressSummaryResponse.getProgressList().size(), AGENDAS_TO_TEST);
    }

    protected AgendaProgress persistTestObject()
    {
        AgendaProgress progress = new AgendaProgress();
        progress.setCustomerId(testCustomerId);
        progress.setLinkId(LINK_ID);
        progress.setProcessingState(ProcessingState.EXECUTING);
        progress = agendaProgressClient.persistObject(progress).getFirst();
        return progress;
    }
}
