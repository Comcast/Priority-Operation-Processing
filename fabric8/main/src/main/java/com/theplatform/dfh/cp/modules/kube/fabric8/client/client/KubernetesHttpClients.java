package com.theplatform.dfh.cp.modules.kube.fabric8.client.client;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.facade.KubernetesClientFacade;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for the different types of clients. This allows the connection tracker to be used in some cases without affecting all sockets.
 */
public class KubernetesHttpClients
{
    private static final String NAME_LOG_WATCH = "LogWatch";
    private static final String NAME_POD_WATCH = "PodWatch";
    private static final String NAME_GENERAL_REQUEST = "GeneralRequest";

    // client for log watch (this one gets reset a lot and has issues with shutdown (socket timeout = 0)
    private KubernetesClientFacade logWatchClient;
    // client for pod watch
    private KubernetesClientFacade podWatchClient;
    // client for simple action requests
    private KubernetesClientFacade requestClient;

    private List<KubernetesClientFacade> kubernetesClients = new ArrayList<>();

    public KubernetesHttpClients(KubernetesClientFacade logWatchClient, KubernetesClientFacade podWatchClient,
        KubernetesClientFacade requestClient)
    {
        this.logWatchClient = logWatchClient;
        this.podWatchClient = podWatchClient;
        this.requestClient = requestClient;

        // setup names
        registerClient(logWatchClient, NAME_LOG_WATCH);
        registerClient(podWatchClient, NAME_POD_WATCH);
        registerClient(requestClient, NAME_GENERAL_REQUEST);
    }

    protected void registerClient(KubernetesClientFacade kubernetesClientFacade, String name)
    {
        kubernetesClientFacade.setName(name);
        kubernetesClientFacade.getConnectionTracker().setName(name);
        kubernetesClients.add(kubernetesClientFacade);
    }

    public void updatePodName(String podName)
    {
        kubernetesClients.forEach(kubernetesClientFacade ->
            kubernetesClientFacade.getConnectionTracker().setPodName(podName));
    }

    public KubernetesClientFacade getLogWatchClient()
    {
        return logWatchClient;
    }

    public KubernetesClientFacade getPodWatchClient()
    {
        return podWatchClient;
    }

    public KubernetesClientFacade getRequestClient()
    {
        return requestClient;
    }

    public void close()
    {
        kubernetesClients.forEach(KubernetesClientFacade::close);
    }
}
