package com.theplatform.dfh.cp.handler.puller.impl.processor;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.endpoint.web.client.api.CPWebClientAPI;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AwsAgendaProviderClient;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerConfig;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 */
public class PullerProcessorTest
{
    @DataProvider
    public Object[][] agendaCases()
    {
        return new Object[][]
            {
                {null, 0},
                {new Agenda(), 1}
            };
    }

    @Test(dataProvider = "agendaCases")
    public void testAgendaClientReturnsNull(Agenda agenda, int executeCalls)
    {
        AwsAgendaProviderClient clientMock = mock(AwsAgendaProviderClient.class);
        when(clientMock.getAgenda()).thenReturn(agenda);

        int pullWait = 0;
        PullerConfig pullerConfig = new PullerConfig();
        pullerConfig.setPullWait(pullWait);

        BaseLauncher launcherMock = mock(BaseLauncher.class);
        PullerLaunchDataWrapper launchDataWrapper = mock(PullerLaunchDataWrapper.class);
        doReturn(pullerConfig).when(launchDataWrapper).getPullerConfig();

        PullerProcessor pullerProcessor = new PullerProcessor();
        pullerProcessor.setAgendaClient(clientMock);
        pullerProcessor.setLauncher(launcherMock);
        pullerProcessor.setLaunchDataWrapper(launchDataWrapper);
        pullerProcessor.execute();

        verify(launcherMock, times(executeCalls)).execute(agenda);
    }
}
