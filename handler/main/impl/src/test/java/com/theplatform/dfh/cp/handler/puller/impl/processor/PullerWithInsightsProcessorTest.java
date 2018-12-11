package com.theplatform.dfh.cp.handler.puller.impl.processor;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AwsAgendaProviderClient;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerConfig;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.executor.BaseLauncher;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
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
 */
public class PullerWithInsightsProcessorTest
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
        GetAgendaResponse getAgendaResponse = new GetAgendaResponse();
        if (agenda != null)
        { 
            getAgendaResponse.setAgendas(Arrays.asList(agenda));
        }
        
        AwsAgendaProviderClient clientMock = mock(AwsAgendaProviderClient.class);
        when(clientMock.getAgenda(any())).thenReturn(getAgendaResponse);

        int pullWait = 0;
        String insightId = UUID.randomUUID().toString();
        PullerConfig pullerConfig = new PullerConfig();
        pullerConfig.setPullWait(pullWait);
        pullerConfig.setInsightId(insightId);

        BaseLauncher launcherMock = mock(BaseLauncher.class);
        PullerLaunchDataWrapper launchDataWrapper = mock(PullerLaunchDataWrapper.class);
        doReturn(pullerConfig).when(launchDataWrapper).getPullerConfig();


        PullerWithInsightProcessor pullerProcessor = new PullerWithInsightProcessor(insightId);
        pullerProcessor.setAgendaClient(clientMock);
        pullerProcessor.setLauncher(launcherMock);
        pullerProcessor.setLaunchDataWrapper(launchDataWrapper);
        pullerProcessor.execute();

        verify(launcherMock, times(executeCalls)).execute(agenda);
    }

}
