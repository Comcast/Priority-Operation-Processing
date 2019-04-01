package com.theplatform.dfh.cp.modules.kube.fabric8.client;

import com.theplatform.dfh.cp.modules.kube.client.LogLineAccumulator;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.LogLineAccumulatorImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodResourceFacade;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodWatcher;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodWatcherImpl;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.RetryablePodResource;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Used to manage pods. Instances should be shared across multiple pods.
 */
public class PodPushClientImpl implements PodPushClient
{
    private static final String START_POD_TEMPLATE = "Starting pod [%s] on node [%s]";
    private static final String DELETE_POD_TEMPLATE = "Deleting pod [%s] on node [%s]";
    public static final String UNDEFINED_NODE_NAME = "Node for pod not defined";
    private static Logger logger = LoggerFactory.getLogger(PodPushClientImpl.class);

    private KubeConfig kubeConfig;
    private DefaultKubernetesClient fabric8Client;

    @Override
    public KubeConfig getKubeConfig()
    {
        return kubeConfig;
    }

    @Override
    public void setKubeConfig(KubeConfig kubeConfig)
    {
        this.kubeConfig = kubeConfig;
    }


    @Override
    public void setFabric8Client(DefaultKubernetesClient fabric8Client)
    {
        this.fabric8Client = fabric8Client;
    }

    @Override
    public DefaultKubernetesClient getFabric8Client()
    {
        return fabric8Client;
    }


    public void close()
    {
        fabric8Client.close();
    }

    /**
     * Asynchronous method. Starts a Pod and returns a Watcher that can provide details for the running Pod
     * @param podConfig
     * @param executionConfig
     * @param podScheduled Always set this count to 1. Countdown is called when pod has been scheduled.
     * @param podFinishedSuccessOrFailure Always set this count to 1. Countdown is called when pod has completed
     */
    public PodWatcher start(PodConfig podConfig, ExecutionConfig executionConfig,
        CountDownLatch podScheduled, CountDownLatch podFinishedSuccessOrFailure)
    {
        if (podConfig == null || executionConfig == null)
        {
            throw new IllegalArgumentException("Must provide configuration details for Pod creation.");
        }
        else if (podConfig.getImageName() == null)
        {
            throw new IllegalArgumentException("Must provide podConfig.imageName.");
        }
        
        Pod podToCreate = Fabric8Helper.getPodSpec(kubeConfig, podConfig, executionConfig);

        if (executionConfig.getLogLineAccumulator() == null)
            executionConfig.setLogLineAccumulator(new LogLineAccumulatorImpl());

        if(!podConfig.isEndOfLogIdentifierEmpty())
        {
            executionConfig.getLogLineAccumulator().setCompletionIdentifier(podConfig.getEndOfLogIdentifier());
        }

        String podName = podToCreate.getMetadata().getName();

        RetryablePodResource podResource = new RetryablePodResource(getPodResource(podName));

        PodWatcherImpl podWatcherImpl = getPodWatcher(
            podScheduled,
            podFinishedSuccessOrFailure,
            podName,
            podResource,
            executionConfig.getLogLineAccumulator());
        Watch watch = initializePodWatcher(podResource, podWatcherImpl);
        podWatcherImpl.setWatch(watch);
        logPodSpecs(startPod(podToCreate), START_POD_TEMPLATE);
        return podWatcherImpl;
    }

    @Override
    public void startWithoutWatcher(PodConfig podConfig, ExecutionConfig executionConfig)
    {
        if (podConfig == null || executionConfig == null)
        {
            throw new IllegalArgumentException("Must provide configuration details for Pod creation.");
        }
        else if (podConfig.getImageName() == null)
        {
            throw new IllegalArgumentException("Must provide podConfig.imageName.");
        }
        Pod podToCreate = Fabric8Helper.getPodSpec(kubeConfig, podConfig, executionConfig);
        logPodSpecs(startPod(podToCreate), START_POD_TEMPLATE);
    }

    private PodWatcherImpl getPodWatcher(CountDownLatch podScheduled, CountDownLatch podFinishedSuccessOrFailure,
        String fullName, PodResourceFacade podResource, LogLineAccumulator logLineAccumulator)
    {
        PodWatcherImpl podWatcherImpl = new PodWatcherImpl();
        podWatcherImpl.setPodName(fullName);
        podWatcherImpl.setScheduledLatch(podScheduled);
        podWatcherImpl.setFinishedLatch(podFinishedSuccessOrFailure);
        podWatcherImpl.setPodResource(podResource);
        podWatcherImpl.setLogLineAccumulator(logLineAccumulator);
        return podWatcherImpl;
    }

    private Pod startPod(Pod podToCreate)
    {
        return fabric8Client.pods().create(podToCreate);
    }

    private void logPodSpecs(Pod pod, String logTemplate)
    {
        PodSpec podSpec = pod.getSpec();
        String nodeName = podSpec.getNodeName();
        nodeName = StringUtils.isEmpty(nodeName)? UNDEFINED_NODE_NAME : nodeName;
        String podName = pod.getMetadata().getName();
        logger.info(String.format(logTemplate,podName, nodeName));
    }

    public void editPodAnnotations(String podName, Map<String, String> annotations)
    {
        DoneablePod pod = fabric8Client.pods().withName(podName).edit();
        pod.editMetadata().addToAnnotations(annotations).and().done();
    }

    private Watch initializePodWatcher(PodResourceFacade podResource, PodWatcherImpl podWatcherImpl)
    {
        return podResource.watch(podWatcherImpl);
    }

    public PodResource<Pod, DoneablePod> getPodResource(String fullName)
    {
        return fabric8Client.pods().inNamespace(kubeConfig.getNameSpace()).withName(fullName);
    }

    public boolean deletePod(String podName)
    {
        try
        {
            logPodSpecs(getPodResource(podName).get(), DELETE_POD_TEMPLATE);
            getPodResource(podName).delete();
            return true;
        }
        catch (Exception e)
        {
            logger.warn("Pod with pod name {} could not be deleted", podName);
            logger.warn("Pod Deletion Error {}: Error Message: {}", podName, e);
            return false;
        }
    }
}