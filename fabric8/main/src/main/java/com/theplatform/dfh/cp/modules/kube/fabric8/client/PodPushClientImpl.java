package com.theplatform.dfh.cp.modules.kube.fabric8.client;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.client.KubernetesHttpClients;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.facade.PodResourceFacadeFactory;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.facade.RetryablePodResourceFacadeFactory;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.logging.LogLineAccumulatorImpl;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.facade.PodResourceFacade;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodWatcher;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodWatcherFactory;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodWatcherImpl;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
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

    private PodWatcherFactory podWatcherFactory = new PodWatcherFactory();
    private PodResourceFacadeFactory podResourceFacadeFactory = new RetryablePodResourceFacadeFactory();
    private KubeConfig kubeConfig;
    private KubernetesHttpClients kubernetesHttpClients;

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
    public KubernetesHttpClients getKubernetesHttpClients()
    {
        return kubernetesHttpClients;
    }

    @Override
    public void setKubernetesHttpClients(KubernetesHttpClients kubernetesHttpClients)
    {
        this.kubernetesHttpClients = kubernetesHttpClients;
    }

    @Override
    public void close()
    {
        kubernetesHttpClients.close();
    }

    /**
     * Asynchronous method. Starts a Pod and returns a Watcher that can provide details for the running Pod
     * @param podConfig
     * @param executionConfig
     * @param podScheduled Always set this count to 1. Countdown is called when pod has been scheduled.
     * @param podFinishedSuccessOrFailure Always set this count to 1. Countdown is called when pod has completed
     */
    @Override
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

        String podName = podToCreate.getMetadata().getName();

        if (executionConfig.getLogLineAccumulator() == null)
            executionConfig.setLogLineAccumulator(new LogLineAccumulatorImpl(podName));

        if(!podConfig.isEndOfLogIdentifierEmpty())
        {
            executionConfig.getLogLineAccumulator().setCompletionIdentifier(podConfig.getEndOfLogIdentifier());
        }

        // log watching (PodWatcherImpl establishes this) via log watch client
        PodWatcherImpl podWatcherImpl = podWatcherFactory.createPodWatcher(
            kubernetesHttpClients.getLogWatchClient(),
            podScheduled,
            podFinishedSuccessOrFailure,
            kubeConfig.getNameSpace(),
            podName,
            executionConfig.getLogLineAccumulator()
        );
        // pod watching (PodWatcherImpl.eventReceived) via pod watch client
        Watch watch = initializePodWatcher(podName, podWatcherImpl);
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
        logger.debug("Master url [" + kubeConfig.getMasterUrl() + "]");
        Pod podToCreate = Fabric8Helper.getPodSpec(kubeConfig, podConfig, executionConfig);
        logPodSpecs(startPod(podToCreate), START_POD_TEMPLATE);
    }

    private Pod startPod(Pod podToCreate)
    {
        PodSpec podSpec = podToCreate.getSpec();
        Map<String, String> selector = podSpec.getNodeSelector();
        if (selector != null && selector.size() > 0)
        {
            String selectorString = "Node selector: ";
            for (String selectorName : selector.keySet())
            {
                selectorString += selectorName + "/" + selector.get(selectorName) + " ";
            }
            logger.debug(selectorString);
        }
        else
        {
            logger.warn("No node selector set");
        }

        return kubernetesHttpClients.getRequestClient().startPod(podToCreate);
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
        kubernetesHttpClients.getRequestClient().updatePodAnnotations(podName, annotations);
    }

    private Watch initializePodWatcher(String podName, PodWatcherImpl podWatcherImpl)
    {
        PodResourceFacade podResourceFacade = podResourceFacadeFactory.create(kubernetesHttpClients.getPodWatchClient(), kubeConfig.getNameSpace(), podName);
        return podResourceFacade.watch(podWatcherImpl);
    }

    public PodPushClientImpl setPodResourceFacadeFactory(PodResourceFacadeFactory podResourceFacadeFactory)
    {
        this.podResourceFacadeFactory = podResourceFacadeFactory;
        return this;
    }

    public boolean deletePod(String podName)
    {
        try
        {
            PodResource<Pod, DoneablePod> podResource = kubernetesHttpClients.getRequestClient().getPodResource(kubeConfig.getNameSpace(), podName);
            if(podResource.get() == null)
            {
                logger.warn("Attempted to delete missing pod [{}]", podName);
                return true;
            }
            logPodSpecs(podResource.get(), DELETE_POD_TEMPLATE);
            podResource.delete();
            return true;
        }
        catch (Exception e)
        {
            logger.warn("Pod Deletion Error {}", podName, e);
            return false;
        }
    }

    public PodPushClientImpl setPodWatcherFactory(PodWatcherFactory podWatcherFactory)
    {
        this.podWatcherFactory = podWatcherFactory;
        return this;
    }
}