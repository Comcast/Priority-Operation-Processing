package com.theplatform.dfh.cp.handler.puller.impl;

import com.theplatform.dfh.cp.handler.field.retriever.properties.PropertyRetriever;
import com.theplatform.dfh.cp.handler.kubernetes.monitor.AliveCheckPollerFactory;
import com.theplatform.dfh.cp.handler.kubernetes.monitor.MetricReporterFactory;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AwsAgendaProviderClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.DefaultAgendaClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerConfig;
import com.theplatform.dfh.cp.handler.puller.impl.healthcheck.AliveHealthCheck;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
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
        AgendaClientFactory agendaClientFactory;
        logger.info("Agenda path: [" + config.getLocalAgendaRelativePath());
        if (config.getLocalAgendaRelativePath() == null)
        {
            logger.info("Using AWS agenda provider");
            agendaClientFactory = new AwsAgendaProviderClientFactory(config);
        }
        else
        {
            logger.info("Using Local agenda provider");
            agendaClientFactory = new DefaultAgendaClientFactory(config.getLocalAgendaRelativePath());
        }
        pullerEntryPoint.setAgendaClientFactory(agendaClientFactory);
        pullerEntryPoint.setPullerConfig(config);

        PullerExecution pullerExecution = new PullerExecution(pullerEntryPoint);

        //Initialize our metric / alert monitoring
        PropertyRetriever propertyRetriever = pullerEntryPoint.getLaunchDataWrapper().getPropertyRetriever();
        MetricReporter metricReporter = MetricReporterFactory.getInstance(propertyRetriever);
        AliveCheckPollerFactory.startInstance(pullerExecution, propertyRetriever, metricReporter);

        pullerEntryPoint.setMetricReporter(metricReporter);

        environment.healthChecks().register("basic-health",
            new AliveHealthCheck(pullerExecution.getExecutionContext())
                .addAliveCheck(pullerEntryPoint.getLaunchDataWrapper().getLastRequestAliveCheck())
        );

        pullerExecution.start();
    }

}
