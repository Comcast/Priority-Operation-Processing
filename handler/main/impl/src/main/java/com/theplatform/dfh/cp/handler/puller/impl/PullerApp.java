package com.theplatform.dfh.cp.handler.puller.impl;

import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerConfig;
import com.theplatform.dfh.cp.handler.puller.impl.healthcheck.AliveHealthCheck;
import com.theplatform.dfh.cp.handler.puller.impl.retriever.PullerArgumentProvider;
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
        PullerExecution pullerExecution = new PullerExecution(pullerEntryPoint);

        environment.healthChecks().register("basic-health", new AliveHealthCheck(pullerExecution.getExecutionContext()));

        pullerExecution.start();
    }


    /**
     * Debugging/running with a local-only build use these args:
     * -launchType local -externalLaunchType local -propFile ./handler/main/package/local/config/external.properties -confPath ./handler/main/package/local/config/conf.yaml
     *
     * Debugging running local with minikube for pod launches
     * -launchType local -externalLaunchType kubernetes -propFile ./handler/main/package/local/config/external-minikube.properties
     *
     * Debugging/running local with kubernetes for pod launches: (requires environment vars for k8s auth)
     * -launchType local -externalLaunchType kubernetes -propFile ./handler/main/package/local/config/external.properties
     *
     * @param args command line args
     */
    public static void main( String[] args ) throws Exception
    {
        PullerEntryPoint pullerEntryPoint = new PullerEntryPoint(args);
        FieldRetriever argumentRetriever = pullerEntryPoint.getLaunchDataWrapper().getArgumentRetriever();
        String confPath = argumentRetriever.getField(PullerArgumentProvider.CONF_PATH, "/app/config/conf.yaml");
        String[] args2 = new String[] {"server", confPath};
        new PullerApp(pullerEntryPoint).run(args2);
    }
}
