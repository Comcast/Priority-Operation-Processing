package com.comcast.pop.endpoint.test.resourcepool.service;

import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.resourcepool.UpdateAgendaProgressRequest;
import com.comcast.pop.endpoint.api.resourcepool.UpdateAgendaProgressResponse;
import com.comcast.pop.endpoint.test.factory.DataGenerator;
import com.comcast.pop.api.AgendaInsight;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.endpoint.test.base.EndpointTestBase;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClient;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UpdateAgendaProgressServiceTest extends EndpointTestBase
{
    @Test
    public void testUpdateAgendaProgress()
    {
        final double INITIAL_PERCENT_COMPLETE = 15;
        final double UPDATED_PERCENT_COMPLETE = 65;

        AgendaInsight agendaInsight = new AgendaInsight();
        agendaInsight.setInsightId(testInsightId);

        // create the object
        AgendaProgress agendaProgress = DataGenerator.getAgendaProgress(testCustomerId);
        agendaProgress.setPercentComplete(INITIAL_PERCENT_COMPLETE);
        agendaProgress.setAgendaInsight(agendaInsight);
        DataObjectResponse<AgendaProgress> persistResponse = agendaProgressClient.persistObject(agendaProgress);
        verifyNoError(persistResponse);
        Assert.assertEquals(persistResponse.getCount().intValue(), 1);
        Assert.assertEquals(persistResponse.getFirst().getPercentComplete(), INITIAL_PERCENT_COMPLETE);

        // update the object
        AgendaProgress updatedAgendaProgress = new AgendaProgress();
        updatedAgendaProgress.setId(persistResponse.getFirst().getId());
        updatedAgendaProgress.setPercentComplete(UPDATED_PERCENT_COMPLETE);
        UpdateAgendaProgressRequest request = new UpdateAgendaProgressRequest();
        request.setAgendaProgress(updatedAgendaProgress);
        resourcePoolServiceClient = new ResourcePoolServiceClient(resourcePoolServiceUrl, getDefaultHttpURLConnectionFactory());
        UpdateAgendaProgressResponse updateResponse = resourcePoolServiceClient.updateAgendaProgress(request);
        verifyNoError(updateResponse);

        // verify the updated object
        persistResponse = agendaProgressClient.getObject(persistResponse.getFirst().getId());
        verifyNoError(persistResponse);
        Assert.assertEquals(persistResponse.getCount().intValue(), 1);
        Assert.assertEquals(persistResponse.getFirst().getPercentComplete(), UPDATED_PERCENT_COMPLETE);
    }
}
