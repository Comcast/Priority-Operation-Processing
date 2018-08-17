package com.theplatform.dfh.cp.modules.kube.fabric8.client;

import com.theplatform.dfh.cp.modules.kube.client.LogLineAccumulator;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.LogLineAccumulatorImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodWatcher;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodWatcherImpl;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Used to manage pods. Instances should be shared across multiple pods.
 */
public class PodPushClientImpl implements PodPushClient
{
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

        PodResource<Pod, DoneablePod> podResource = getPodResource(podName);

        PodWatcherImpl podWatcherImpl = getPodWatcher(
            podScheduled,
            podFinishedSuccessOrFailure,
            podName,
            podResource,
            executionConfig.getLogLineAccumulator());
        Watch watch = initializePodWatcher(podResource, podWatcherImpl);
        podWatcherImpl.setWatch(watch);
        startPod(podToCreate);
        logger.info("Created Pod: " + podName);
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

        String podName = podToCreate.getMetadata().getName();
        logger.info("Created Pod: " + podName);
        startPod(podToCreate);
    }

    private PodWatcherImpl getPodWatcher(CountDownLatch podScheduled, CountDownLatch podFinishedSuccessOrFailure,
        String fullName, PodResource<Pod, DoneablePod> podResource, LogLineAccumulator logLineAccumulator)
    {
        PodWatcherImpl podWatcherImpl = new PodWatcherImpl();
        podWatcherImpl.setPodName(fullName);
        podWatcherImpl.setScheduledLatch(podScheduled);
        podWatcherImpl.setFinishedLatch(podFinishedSuccessOrFailure);
        podWatcherImpl.setPodClient(podResource);
        podWatcherImpl.setLogLineAccumulator(logLineAccumulator);
        return podWatcherImpl;
    }

    private void  startPod(Pod podToCreate)
    {
        fabric8Client.pods().create(podToCreate);
    }

    public void editPodAnnotations(String podName, Map<String, String> annotations)
    {
        DoneablePod pod = fabric8Client.pods().withName(podName).edit();
        pod.editMetadata().addToAnnotations(annotations).and().done();
    }

    private Watch initializePodWatcher(PodResource<Pod, DoneablePod> podResource, PodWatcherImpl podWatcherImpl)
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
            getPodResource(podName).delete();
            logger.info("Pod with pod name {} deleted.", podName);
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