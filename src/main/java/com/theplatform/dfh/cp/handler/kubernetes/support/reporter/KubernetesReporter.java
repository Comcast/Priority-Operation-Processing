package com.theplatform.dfh.cp.handler.kubernetes.support.reporter;

import com.theplatform.dfh.cp.handler.base.reporter.ProgressReporter;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.Fabric8Helper;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.annotation.PodAnnotationClient;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.facade.RetryableKubernetesClient;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic kubernetes reporter (uses kubernetes pod annotations)
 */
public class KubernetesReporter<T> implements ProgressReporter<T>
{
    public static final String REPORT_PROGRESS_ANNOTATION = "dfh.report.progress";
    public static final String REPORT_PAYLOAD_ANNOTATION = "dfh.report.payload";

    private PodAnnotationClient podAnnotationClient;
    private JsonHelper jsonHelper;

    public KubernetesReporter()
    {
        this.jsonHelper = new JsonHelper();
    }

    public KubernetesReporter(KubeConfig kubeConfig, String podName)
    {
        this();
        podAnnotationClient = new PodAnnotationClient(
            new RetryableKubernetesClient(new DefaultKubernetesClient(Fabric8Helper.getFabric8Config(kubeConfig))),
            podName);
    }

    @Override
    public void reportProgress(T object)
    {
        updatePodAnotations(Collections.singletonMap(REPORT_PROGRESS_ANNOTATION, jsonHelper.getJSONString(object)));
    }

    @Override
    public void reportProgress(T object, Object resultPayload)
    {
        Map<String, String> annotationMap = new HashMap<>();
        annotationMap.put(REPORT_PROGRESS_ANNOTATION, jsonHelper.getJSONString(object));
        annotationMap.put(REPORT_PAYLOAD_ANNOTATION, jsonHelper.getJSONString(resultPayload));
        updatePodAnotations(annotationMap);
    }

    protected void updatePodAnotations(Map<String, String> updatedAnnotations)
    {
        if(podAnnotationClient == null)
        {
            throw new RuntimeException("KubernetesReporter requires a PodAnnotationClient.");
        }

        podAnnotationClient.editPodAnnotations(updatedAnnotations);
    }

    public void setPodAnnotationClient(PodAnnotationClient podAnnotationClient)
    {
        this.podAnnotationClient = podAnnotationClient;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }
}
