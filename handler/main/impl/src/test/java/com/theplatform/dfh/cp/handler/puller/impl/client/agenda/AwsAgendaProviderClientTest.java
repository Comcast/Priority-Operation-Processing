package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.endpoint.client.HttpCPWebClient;
import org.testng.Assert;
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

    @Test
    public void testGetAgenda()
    {
        Agenda agenda = new Agenda();
        agenda.setId(UUID.randomUUID().toString());
        HttpCPWebClient webClient = mock(HttpCPWebClient.class);
        when(webClient.getAgenda()).thenReturn(agenda);

        AwsAgendaProviderClient awsClient = new AwsAgendaProviderClient(webClient);

        Agenda agendaResponse = awsClient.getAgenda();
        String expectedAgenda =jsonHelper.getJSONString(agenda);
        String agendaResponseJson = jsonHelper.getJSONString(agendaResponse);
        Assert.assertEquals(agendaResponseJson, expectedAgenda);
        verify(webClient, times(1)).getAgenda();
    }

    @Test
    public void testGetAgendaWithInsight()
    {
        Agenda agenda = new Agenda();
        agenda.setId(UUID.randomUUID().toString());
        GetAgendaResponse expectedResponse = new GetAgendaResponse(Arrays.asList(agenda));

        HttpCPWebClient webClient = mock(HttpCPWebClient.class);
        when(webClient.getAgenda(any())).thenReturn(expectedResponse);

        AwsAgendaProviderClient awsClient = new AwsAgendaProviderClient(webClient);

        GetAgendaRequest getAgendaRequest = new GetAgendaRequest("foo", 1);
        GetAgendaResponse agendaResponse = awsClient.getAgenda(getAgendaRequest);
        String expectedAgenda =jsonHelper.getJSONString(expectedResponse);
        String agendaResponseJson = jsonHelper.getJSONString(agendaResponse);
        Assert.assertEquals(agendaResponseJson, expectedAgenda);
        verify(webClient, times(1)).getAgenda(getAgendaRequest);
    }

    @Test
    public void testGetAgendaReturnsNull()
    {
        HttpCPWebClient webClient = mock(HttpCPWebClient.class);
        when(webClient.getAgenda()).thenReturn(null);

        AwsAgendaProviderClient awsClient = new AwsAgendaProviderClient(webClient);

        Agenda agendaResponse = awsClient.getAgenda();
        Assert.assertNull(agendaResponse);
        verify(webClient, times(1)).getAgenda();
    }
}
