package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.endpoint.client.FissionClient;
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
    private FissionClient mockFissionClient;

    @BeforeMethod
    public void setup()
    {
        mockFissionClient = mock(FissionClient.class);
    }

    @Test
    public void testGetAgenda()
    {
        Agenda agenda = new Agenda();
        agenda.setId(UUID.randomUUID().toString());
        when(mockFissionClient.getAgenda()).thenReturn(agenda);

        AwsAgendaProviderClient awsClient = new AwsAgendaProviderClient(mockFissionClient);

        Agenda agendaResponse = awsClient.getAgenda();
        String expectedAgenda =jsonHelper.getJSONString(agenda);
        String agendaResponseJson = jsonHelper.getJSONString(agendaResponse);
        Assert.assertEquals(agendaResponseJson, expectedAgenda);
        verify(mockFissionClient, times(1)).getAgenda();
    }

    @Test
    public void testGetAgendaWithInsight()
    {
        Agenda agenda = new Agenda();
        agenda.setId(UUID.randomUUID().toString());
        GetAgendaResponse expectedResponse = new GetAgendaResponse(Arrays.asList(agenda));

        when(mockFissionClient.getAgenda(any())).thenReturn(expectedResponse);

        AwsAgendaProviderClient awsClient = new AwsAgendaProviderClient(mockFissionClient);

        GetAgendaRequest getAgendaRequest = new GetAgendaRequest("foo", 1);
        GetAgendaResponse agendaResponse = awsClient.getAgenda(getAgendaRequest);
        String expectedAgenda =jsonHelper.getJSONString(expectedResponse);
        String agendaResponseJson = jsonHelper.getJSONString(agendaResponse);
        Assert.assertEquals(agendaResponseJson, expectedAgenda);
        verify(mockFissionClient, times(1)).getAgenda(getAgendaRequest);
    }

    @Test
    public void testGetAgendaReturnsNull()
    {
        when(mockFissionClient.getAgenda()).thenReturn(null);

        AwsAgendaProviderClient awsClient = new AwsAgendaProviderClient(mockFissionClient);

        Agenda agendaResponse = awsClient.getAgenda();
        Assert.assertNull(agendaResponse);
        verify(mockFissionClient, times(1)).getAgenda();
    }
}
