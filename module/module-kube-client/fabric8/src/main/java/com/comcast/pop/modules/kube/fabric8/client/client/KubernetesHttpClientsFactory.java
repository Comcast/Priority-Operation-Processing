package com.comcast.pop.modules.kube.fabric8.client.client;

import com.comcast.pop.modules.kube.fabric8.client.facade.KubernetesClientFacade;
import com.comcast.pop.modules.kube.fabric8.client.facade.RetryableKubernetesClient;
import com.comcast.pop.modules.kube.fabric8.client.http.HttpClientFactory;
import com.comcast.pop.modules.kube.fabric8.client.watcher.ConnectionTracker;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

/**
 * Creates the KubernetesHttpClients object with independent clients
 */
public class KubernetesHttpClientsFactory
{
    private HttpClientFactory httpClientFactory = new HttpClientFactory();

    public KubernetesHttpClients createClients(Config config)
    {
        return new KubernetesHttpClients(
            createRetryableKubernetesClient(config, new ConnectionTracker()),
            createRetryableKubernetesClient(config, new ConnectionTracker()),
            createRetryableKubernetesClient(config, new ConnectionTracker())
        );
    }

    protected KubernetesClientFacade createRetryableKubernetesClient(Config config, ConnectionTracker connectionTracker)
    {
        return
            new RetryableKubernetesClient(
                new DefaultKubernetesClient(httpClientFactory.createHttpClient(config, connectionTracker), config),
                connectionTracker);
    }

    public KubernetesHttpClientsFactory setHttpClientFactory(HttpClientFactory httpClientFactory)
    {
        this.httpClientFactory = httpClientFactory;
        return this;
    }

}
