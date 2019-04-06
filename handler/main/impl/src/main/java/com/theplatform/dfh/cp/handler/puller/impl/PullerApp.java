package com.theplatform.dfh.cp.handler.puller.impl;

import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AwsAgendaProviderClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerConfig;
import com.theplatform.dfh.cp.handler.puller.impl.healthcheck.AliveHealthCheck;
import com.theplatform.dfh.cp.handler.puller.impl.monitor.alive.LastRequestAliveCheck;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullerApp extends Application<PullerConfig>
{
    private static Logger logger = LoggerFactory.getLogger(PullerApp.class);

    private PullerEntryPoint pullerEntryPoint;

    public PullerApp(PullerEntryPoint pullerEntryPoint)
    {
        this.pullerEntryPoint = pullerEntryPoint;
    }

    @Override
    public void run(PullerConfig config, Environment environment) throws Exception
    {
        AwsAgendaProviderClientFactory agendaClientFactory = new AwsAgendaProviderClientFactory(config);
        pullerEntryPoint.setAgendaClientFactory(agendaClientFactory);
        pullerEntryPoint.setPullerConfig(config);
        PullerExecution pullerExecution = new PullerExecution(pullerEntryPoint);

        environment.healthChecks().register("basic-health",
            new AliveHealthCheck(pullerExecution.getExecutionContext())
                .addAliveCheck(pullerEntryPoint.getLaunchDataWrapper().getLastRequestAliveCheck())
        );

        pullerExecution.start();
    }
}
