package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.cp.handler.puller.impl.PullerApp;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerConfig;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClient;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClientFactory;
import com.theplatform.dfh.http.idm.IDMHTTPClientConfig;
import com.theplatform.dfh.http.idm.IDMHTTPUrlConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The puller has 2 modes of operation and thus a custom client factory
 * 1) with an input file specified that is returned with every getAgenda call
 * 2) actual url connection to the getAgenda method
 */
public class PullerResourcePoolServiceClientFactory
{
    private static Logger logger = LoggerFactory.getLogger(PullerApp.class);
    private PullerConfig pullerConfig;
    private ResourcePoolServiceClientFactory resourcePoolServiceClientFactory;

    public PullerResourcePoolServiceClientFactory(PullerConfig config)
    {
        this.pullerConfig = config;
        this.resourcePoolServiceClientFactory = new ResourcePoolServiceClientFactory();
    }

    public ResourcePoolServiceClient getClient()
    {
        // If there's a local agenda file path, it means we are using that
        // local file as input, not calling the agenda service.
        if (pullerConfig.getLocalAgendaRelativePath() != null)
        {
            return getLocalFileClient();
        }
        return getHTTPServiceClient();
    }

    private ResourcePoolServiceClient getLocalFileClient()
    {
        logger.info("AgendaClientFactory: Using Local agenda provider [" +
            pullerConfig.getLocalAgendaRelativePath() + "]");
        return new LocalResourcePoolServiceClient(pullerConfig.getLocalAgendaRelativePath());
    }

    private ResourcePoolServiceClient getHTTPServiceClient()
    {
        logger.debug("AgendaClientFactory: URL: [" + pullerConfig.getIdentityUrl() +
            "], Username: [" + pullerConfig.getUsername());

        IDMHTTPClientConfig idmhttpClientConfig = new IDMHTTPClientConfig();
        idmhttpClientConfig.setIdentityUrl(pullerConfig.getIdentityUrl());
        idmhttpClientConfig.setUsername(pullerConfig.getUsername());
        idmhttpClientConfig.setEncryptedPassword(pullerConfig.getEncryptedPassword());

        // Verify if the proxy host/port is defined in the configuration. This
        // is required when we need to access the agenda service in a different
        // network zone, such as the green zone when running in RDEI
        // Example proxyHost: 'greenproxy-po-vip.sys.comcast.net'
        // Example proxyPort: '3128'
        // Proxies are documented here:
        //   https://wiki.sys.comcast.net/pages/viewpage.action?spaceKey=ContentOperations&title=Proxies
        if (pullerConfig.getProxyHost() != null && pullerConfig.getProxyPort() != null)
        {
            logger.info("AgendaClientFactory: Using AWS Agenda provider with proxy [" + pullerConfig.getProxyHost() +
                ":" + pullerConfig.getProxyPort() + "]");
            idmhttpClientConfig.setProxyHost(pullerConfig.getProxyHost());
            idmhttpClientConfig.setProxyPort(pullerConfig.getProxyPort());
        }

        logger.debug("AgendaClientFactory: Identity URL: [" + pullerConfig.getIdentityUrl() +
            "], Username: [" + pullerConfig.getUsername() +
            "AgendaProviderURL: " + pullerConfig.getAgendaProviderUrl());


        return resourcePoolServiceClientFactory.create(pullerConfig.getAgendaProviderUrl(),
            new IDMHTTPUrlConnectionFactory(idmhttpClientConfig));
    }

    public PullerResourcePoolServiceClientFactory setPullerConfig(PullerConfig pullerConfig)
    {
        this.pullerConfig = pullerConfig;
        return this;
    }

    public PullerResourcePoolServiceClientFactory setResourcePoolServiceClientFactory(
        ResourcePoolServiceClientFactory resourcePoolServiceClientFactory)
    {
        this.resourcePoolServiceClientFactory = resourcePoolServiceClientFactory;
        return this;
    }
}