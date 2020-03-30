package com.comcast.fission.handler.puller.impl;

import com.theplatform.dfh.cp.handler.base.field.retriever.properties.PropertyRetriever;
import com.theplatform.dfh.cp.handler.kubernetes.support.monitor.AliveCheckPollerFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.monitor.MetricReporterFactory;
import com.comcast.fission.handler.puller.impl.config.PullerConfig;
import com.comcast.fission.handler.puller.impl.healthcheck.AliveHealthCheck;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricFilterBuilder;
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
        pullerEntryPoint.setPullerConfig(config);

        PullerExecution pullerExecution = new PullerExecution(pullerEntryPoint);

        //Initialize our metric / alert monitoring
        PropertyRetriever propertyRetriever = pullerEntryPoint.getLaunchDataWrapper().getPropertyRetriever();
        MetricFilterBuilder.MetricFilter metricFilter = new MetricFilterBuilder().filterCountZero().filterTimer().build();
        MetricReporter metricReporter = MetricReporterFactory.getInstance(propertyRetriever, metricFilter);
        AliveCheckPollerFactory.startInstance(pullerExecution, propertyRetriever, metricReporter);

        pullerEntryPoint.setMetricReporter(metricReporter);

        environment.healthChecks().register("basic-health",
            new AliveHealthCheck(pullerExecution.getExecutionContext())
                .addAliveCheck(pullerEntryPoint.getLaunchDataWrapper().getLastRequestAliveCheck())
        );

        pullerExecution.start();
    }
}
