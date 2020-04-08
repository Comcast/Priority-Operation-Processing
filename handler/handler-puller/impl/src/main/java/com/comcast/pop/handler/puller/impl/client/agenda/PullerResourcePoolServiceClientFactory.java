package com.comcast.pop.handler.puller.impl.client.agenda;

import com.comcast.pop.handler.puller.impl.config.PullerConfigField;
import com.comcast.pop.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClient;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClientFactory;
import com.theplatform.dfh.http.api.AuthHttpURLConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The puller has 2 modes of operation and thus a custom client factory
 * 1) with an input file specified that is returned with every getAgenda call
 * 2) actual url connection to the getAgenda method
 */
public class PullerResourcePoolServiceClientFactory
{
    private static Logger logger = LoggerFactory.getLogger(PullerResourcePoolServiceClientFactory.class);
    private ResourcePoolServiceClientFactory resourcePoolServiceClientFactory;
    private PullerLaunchDataWrapper launchDataWrapper;

    public PullerResourcePoolServiceClientFactory(PullerLaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
        this.resourcePoolServiceClientFactory = new ResourcePoolServiceClientFactory();
    }



    public ResourcePoolServiceClient getClient()
    {
        // If there's a local agenda file path, it means we are using that
        // local file as input, not calling the agenda service.
        if (launchDataWrapper.getPropertyRetriever().getField(PullerConfigField.LOCAL_AGENDA_RELATIVE_PATH) != null)
        {
            return getLocalFileClient();
        }
        return getHTTPServiceClient();
    }

    private ResourcePoolServiceClient getLocalFileClient()
    {
        String localAgendaPath = launchDataWrapper.getPropertyRetriever().getField(PullerConfigField.LOCAL_AGENDA_RELATIVE_PATH);
        logger.info("AgendaClientFactory: Using Local agenda provider [" + localAgendaPath + "]");
        return new LocalResourcePoolServiceClient(localAgendaPath);
    }

    private ResourcePoolServiceClient getHTTPServiceClient()
    {
        return resourcePoolServiceClientFactory.create(launchDataWrapper.getPropertyRetriever().getField(PullerConfigField.POP_RESOURCE_POOL_SERVICE_URL),
            new AuthHttpURLConnectionFactory());
    }

    public PullerResourcePoolServiceClientFactory setResourcePoolServiceClientFactory(
        ResourcePoolServiceClientFactory resourcePoolServiceClientFactory)
    {
        this.resourcePoolServiceClientFactory = resourcePoolServiceClientFactory;
        return this;
    }
}