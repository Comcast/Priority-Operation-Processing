package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.endpoint.web.client.api.CPWebClientAPI;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AwsAgendaProviderClient;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

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
        CPWebClientAPI webClient = mock(CPWebClientAPI.class);
        when(webClient.getAgenda()).thenReturn(agenda);

        AwsAgendaProviderClient awsClient = new AwsAgendaProviderClient(webClient);

        String agendaResponse = awsClient.getAgenda();
        String expectedAgenda =jsonHelper.getJSONString(agenda);
        Assert.assertEquals(agendaResponse, expectedAgenda);
        verify(webClient, times(1)).getAgenda();
    }

    @Test
    public void testGetAgendaReturnsNull()
    {
        CPWebClientAPI webClient = mock(CPWebClientAPI.class);
        when(webClient.getAgenda()).thenReturn(null);

        AwsAgendaProviderClient awsClient = new AwsAgendaProviderClient(webClient);

        String agendaResponse = awsClient.getAgenda();
        Assert.assertNull(agendaResponse);
        verify(webClient, times(1)).getAgenda();
    }
}
