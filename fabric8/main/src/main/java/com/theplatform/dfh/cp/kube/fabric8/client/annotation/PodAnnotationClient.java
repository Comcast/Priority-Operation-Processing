package com.theplatform.dfh.cp.kube.fabric8.client.annotation;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import java.util.Map;


/**
 *  Edit and access annotations for a given kubernetes pod
 */
public class PodAnnotationClient
{

    private String podName;
    private DefaultKubernetesClient fabric8Client;

    PodAnnotationClient(DefaultKubernetesClient fabric8Client, String podName)
    {
        this.fabric8Client = fabric8Client;
        this.podName = podName;
    }

    /**
     * Edit annotations by overwriting any intersecting key names, but preserving non-intersecting key names.
     *
     * @param annotations new annotations to apply to existing (with precedence given to new).
     */
    public void editPodAnnotations(Map<String, String> annotations)
    {
        DoneablePod pod = fabric8Client.pods().withName(podName).edit();
        pod.editMetadata().addToAnnotations(annotations).and().done();
    }

    /**
     * @return annotations for pod with podName
     */
    public Map<String, String> getPodAnnotations()
    {
        DoneablePod pod = fabric8Client.pods().withName(podName).edit();
        return pod.buildMetadata().getAnnotations();
    }

    public String getPodName()
    {
        return podName;
    }

    public void setPodName(String podName)
    {
        this.podName = podName;
    }

    public DefaultKubernetesClient getFabric8Client()
    {
        return fabric8Client;
    }

    public void setFabric8Client(DefaultKubernetesClient fabric8Client)
    {
        this.fabric8Client = fabric8Client;
    }
}
