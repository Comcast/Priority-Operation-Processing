package com.theplatform.dfh.cp.handler.puller.impl.processor;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.PullerResourcePoolServiceClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerConfig;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.cp.handler.puller.impl.executor.LauncherFactory;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaResponse;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClient;
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
        PullerConfig pullerConfig = new PullerConfig();
        pullerConfig.setPullWait(pullWait);
        pullerConfig.setInsightId(insightId);

        BaseLauncher launcherMock = mock(BaseLauncher.class);
        PullerLaunchDataWrapper launchDataWrapper = mock(PullerLaunchDataWrapper.class);
        doReturn(pullerConfig).when(launchDataWrapper).getPullerConfig();
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
