package com.comcast.pop.handler.puller.impl.processor;

import com.comast.pop.handler.base.field.retriever.properties.PropertyRetriever;
import com.comcast.pop.handler.puller.impl.client.agenda.PullerResourcePoolServiceClientFactory;
import com.comcast.pop.handler.puller.impl.config.PullerConfigField;
import com.comcast.pop.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.handler.puller.impl.executor.BaseLauncher;
import com.comcast.pop.handler.puller.impl.executor.LauncherFactory;
import com.comcast.pop.endpoint.api.resourcepool.GetAgendaResponse;
import com.comcast.pop.endpoint.client.ResourcePoolServiceClient;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class PullerProcessorTest
{
    @DataProvider
    public Object[][] agendaCases()
    {
        return new Object[][]
            {
                {null, 0},
                {createAgenda(UUID.randomUUID().toString()), 1}
            };
    }

    @Test(dataProvider = "agendaCases")
    public void testAgendaClientReturnsNull(Agenda agenda, int executeCalls)
    {
        GetAgendaResponse getAgendaResponse = new GetAgendaResponse();
        if (agenda != null)
        {
            getAgendaResponse.setAgendas(Arrays.asList(agenda));
        }

        ResourcePoolServiceClient clientMock = mock(ResourcePoolServiceClient.class);
        when(clientMock.getAgenda(any())).thenReturn(getAgendaResponse);
        PullerResourcePoolServiceClientFactory clientFactoryMock = mock(PullerResourcePoolServiceClientFactory.class);
        when(clientFactoryMock.getClient()).thenReturn(clientMock);

        int pullWait = 0;
        String insightId = UUID.randomUUID().toString();

        BaseLauncher launcherMock = mock(BaseLauncher.class);
        PullerLaunchDataWrapper launchDataWrapper = mock(PullerLaunchDataWrapper.class);
        PropertyRetriever mockPropertyRetriever = mock(PropertyRetriever.class);
        doReturn(pullWait).when(mockPropertyRetriever).getInt(PullerConfigField.PULL_WAIT, 30);
        doReturn(insightId).when(mockPropertyRetriever).getField(PullerConfigField.INSIGHT_ID);
        LauncherFactory launcherFactoryMock = mock(LauncherFactory.class);
        doReturn(launcherMock).when(launcherFactoryMock).createLauncher(any());

        PullerProcessor pullerProcessor = new PullerProcessor(insightId);
        pullerProcessor.setPullWaitSeconds(pullWait);
        pullerProcessor.setResourcePoolServiceClientFactory(clientFactoryMock);
        pullerProcessor.setLauncherFactory(launcherFactoryMock);
        pullerProcessor.setLaunchDataWrapper(launchDataWrapper);
        pullerProcessor.performAgendaRequest();

        verify(launcherMock, times(executeCalls)).execute(agenda, null);
    }

    protected Agenda createAgenda(String agendaId)
    {
        Agenda agenda = new Agenda();
        agenda.setId(agendaId);
        agenda.setProgressId(UUID.randomUUID().toString());
        return agenda;
    }
}
