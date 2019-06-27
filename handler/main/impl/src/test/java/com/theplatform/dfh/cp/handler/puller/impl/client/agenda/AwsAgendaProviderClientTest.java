package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AwsAgendaProviderClient;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.endpoint.client.AgendaServiceClient;
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
    private AgendaServiceClient agendaServiceClient;

    @BeforeMethod
    public void setup()
    {
        agendaServiceClient = mock(AgendaServiceClient.class);
    }

    @Test
    public void testGetAgendaWithInsight()
    {
        Agenda agenda = new Agenda();
        agenda.setId(UUID.randomUUID().toString());
        GetAgendaResponse expectedResponse = new GetAgendaResponse(Arrays.asList(agenda));

        when(agendaServiceClient.getAgenda(any())).thenReturn(expectedResponse);

        AwsAgendaProviderClient awsClient = new AwsAgendaProviderClient(agendaServiceClient);

        GetAgendaRequest getAgendaRequest = new GetAgendaRequest("foo", 1);
        GetAgendaResponse agendaResponse = awsClient.getAgenda(getAgendaRequest);
        String expectedAgenda =jsonHelper.getJSONString(expectedResponse);
        String agendaResponseJson = jsonHelper.getJSONString(agendaResponse);
        Assert.assertEquals(agendaResponseJson, expectedAgenda);
        verify(agendaServiceClient, times(1)).getAgenda(getAgendaRequest);
    }

    @Test
    public void testGetAgendaReturnsNull()
    {
        when(agendaServiceClient.getAgenda((any()))).thenReturn(null);

        AwsAgendaProviderClient awsClient = new AwsAgendaProviderClient(agendaServiceClient);

        GetAgendaResponse agendaResponse = awsClient.getAgenda(new GetAgendaRequest());
        Assert.assertNull(agendaResponse);
        verify(agendaServiceClient, times(1)).getAgenda(any());
    }

    @Test
    public void testGetAgendaResturnsError()
    {
        GetAgendaResponse response = new GetAgendaResponse(ErrorResponseFactory.objectNotFound("Could not find Insight.", "cid"));
        when(agendaServiceClient.getAgenda(any())).thenReturn(response);

        AwsAgendaProviderClient awsClient = new AwsAgendaProviderClient(agendaServiceClient);

        GetAgendaRequest getAgendaRequest = new GetAgendaRequest("foo", 1);
        GetAgendaResponse agendaResponse = awsClient.getAgenda(getAgendaRequest);
        String expectedAgenda =jsonHelper.getJSONString(response);
        String agendaResponseJson = jsonHelper.getJSONString(agendaResponse);
        Assert.assertEquals(agendaResponseJson, expectedAgenda);
        verify(agendaServiceClient, times(1)).getAgenda(getAgendaRequest);
    }
}
