package com.theplatform.dfh.cp.modules.kube.fabric8.client.annotation;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.facade.KubernetesClientFacade;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import java.util.Map;


/**
 *  Access and add annotations for a given kubernetes pod
 */
public class PodAnnotationClient
{

    private String podName;
    private KubernetesClientFacade kubernetesClient;

    public PodAnnotationClient(KubernetesClientFacade kubernetesClient, String podName)
    {
        this.kubernetesClient = kubernetesClient;
        this.podName = podName;
    }

    /**
     * Edit annotations by overwriting any intersecting key names, but preserving non-intersecting key names.
     *
     * @param annotations new annotations to apply to existing (with precedence given to new).
     */
    public void editPodAnnotations(Map<String, String> annotations)
    {
        kubernetesClient.updatePodAnnotations(podName, annotations);
    }

    /**
     * @return annotations for pod with podName
     */
    public Map<String, String> getPodAnnotations()
    {
        return kubernetesClient.getPodAnnotations(podName);
    }

    public String getPodName()
    {
        return podName;
    }

    public void setPodName(String podName)
    {
        this.podName = podName;
    }
}
