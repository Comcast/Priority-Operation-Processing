package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.cp.handler.puller.impl.PullerApp;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerConfig;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.idm.IDMHTTPUrlConnectionFactory;
import com.theplatform.module.authentication.client.EncryptedAuthenticationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgendaClientFactory
{
    private static Logger logger = LoggerFactory.getLogger(PullerApp.class);
    private PullerConfig pullerConfig;

    public AgendaClientFactory(PullerConfig config)
    {
        this.pullerConfig = config;
    }

    public AgendaClient getClient()
    {
        AgendaClient client;
        // If there's a local agenda file path, it means we are using that
        // local file as input, not calling the agenda service.
        if (pullerConfig.getLocalAgendaRelativePath() == null)
        {
            // Verify if the proxy host/port is defined in the configuration. This
            // is required when we need to access the agenda service in a different
            // network zone, such as the green zone when running in RDEI
            // Example proxyHost: 'greenproxy-po-vip.sys.comcast.net'
            // Example proxyPort: '3128'
            // Proxies are documented here:
            //   https://wiki.sys.comcast.net/pages/viewpage.action?spaceKey=ContentOperations&title=Proxies
            if (pullerConfig.getProxyHost() != null && pullerConfig.getProxyPort() != null)
            {
                logger.info("AgendaClientFactory: Using Proxy [" + pullerConfig.getProxyHost() +
                                    ":" + pullerConfig.getProxyPort() + "]");
                System.setProperty("https.proxyHost", pullerConfig.getProxyHost());
                System.setProperty("https.proxyPort", pullerConfig.getProxyPort());
                System.setProperty("http.proxyHost", pullerConfig.getProxyHost());
                System.setProperty("http.proxyPort", pullerConfig.getProxyPort());
            }
            else
            {
                logger.info("AgendaClientFactory: Using AWS Agenda provider");
            }

            logger.debug("AgendaClientFactory: URL: [" + pullerConfig.getIdentityUrl() +
                                "], Username: [" + pullerConfig.getUsername());

            EncryptedAuthenticationClient authClient =
                    new EncryptedAuthenticationClient(pullerConfig.getIdentityUrl(),
                                                      pullerConfig.getUsername(),
                                                      pullerConfig.getEncryptedPassword(),
                                                      null);

            HttpURLConnectionFactory httpURLConnectionFactory =
                    new IDMHTTPUrlConnectionFactory(authClient);

            client =  new AwsAgendaProviderClient(pullerConfig.getAgendaProviderUrl(), httpURLConnectionFactory);
        }
        else
        {
            logger.info("AgendaClientFactory: Using Local agenda provider [" +
                                pullerConfig.getLocalAgendaRelativePath() + "]");
            client = new LocalAgendaProviderClient(pullerConfig.getLocalAgendaRelativePath());
        }

        return client;
    }
}