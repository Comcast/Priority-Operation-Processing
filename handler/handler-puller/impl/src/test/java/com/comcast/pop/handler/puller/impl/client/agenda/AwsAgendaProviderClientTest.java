package com.comcast.pop.handler.puller.impl.client.agenda;

import com.comcast.pop.api.Agenda;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.resourcepool.GetAgendaRequest;
import com.comcast.pop.endpoint.api.resourcepool.GetAgendaResponse;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClient;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 */
public class AwsAgendaProviderClientTest
{
    private JsonHelper jsonHelper = new JsonHelper();
    private ResourcePoolServiceClient resourcePoolServiceClient;

    @BeforeMethod
    public void setup()
    {
        resourcePoolServiceClient = mock(ResourcePoolServiceClient.class);
    }

    @Test
    public void testGetAgendaWithInsight()
    {
        Agenda agenda = new Agenda();
        agenda.setId(UUID.randomUUID().toString());
        GetAgendaResponse expectedResponse = new GetAgendaResponse(Arrays.asList(agenda));

        when(resourcePoolServiceClient.getAgenda(any())).thenReturn(expectedResponse);

        GetAgendaRequest getAgendaRequest = new GetAgendaRequest("foo", 1);
        GetAgendaResponse agendaResponse = resourcePoolServiceClient.getAgenda(getAgendaRequest);
        String expectedAgenda =jsonHelper.getJSONString(expectedResponse);
        String agendaResponseJson = jsonHelper.getJSONString(agendaResponse);
        Assert.assertEquals(agendaResponseJson, expectedAgenda);
        verify(resourcePoolServiceClient, times(1)).getAgenda(getAgendaRequest);
    }

    @Test
    public void testGetAgendaReturnsNull()
    {
        when(resourcePoolServiceClient.getAgenda((any()))).thenReturn(null);

        GetAgendaResponse agendaResponse = resourcePoolServiceClient.getAgenda(new GetAgendaRequest());
        Assert.assertNull(agendaResponse);
        verify(resourcePoolServiceClient, times(1)).getAgenda(any());
    }

    @Test
    public void testGetAgendaResturnsError()
    {
        GetAgendaResponse response = new GetAgendaResponse(ErrorResponseFactory.objectNotFound("Could not find Insight.", "cid"));
        when(resourcePoolServiceClient.getAgenda(any())).thenReturn(response);

        GetAgendaRequest getAgendaRequest = new GetAgendaRequest("foo", 1);
        GetAgendaResponse agendaResponse = resourcePoolServiceClient.getAgenda(getAgendaRequest);
        String expectedAgenda =jsonHelper.getJSONString(response);
        String agendaResponseJson = jsonHelper.getJSONString(agendaResponse);
        Assert.assertEquals(agendaResponseJson, expectedAgenda);
        verify(resourcePoolServiceClient, times(1)).getAgenda(getAgendaRequest);
    }
}
