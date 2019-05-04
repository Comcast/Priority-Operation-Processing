package com.theplatform.dfh.cp.handler.reaper.impl.processor;

import com.theplatform.dfh.cp.handler.reaper.impl.filter.PodLookupFilter;
import com.theplatform.dfh.cp.handler.reaper.impl.kubernetes.KubernetesPodFacade;
import com.theplatform.dfh.cp.handler.reaper.impl.messages.ReaperMessages;
import io.fabric8.kubernetes.api.model.Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Pod reaper that reaps in batches from both the source and when deleting
 */
public class BatchedPodReaper
{
    private static Logger logger = LoggerFactory.getLogger(ReaperProcessor.class);
    public static final int DEFAULT_POD_REAP_BATCH_SIZE = 50;

    private final PodLookupFilter podLookupFilter;
    private final KubernetesPodFacade kubernetesPodFacade;
    private int podReapBatchSize = DEFAULT_POD_REAP_BATCH_SIZE;

    public BatchedPodReaper(PodLookupFilter podLookupFilter, KubernetesPodFacade kubernetesPodFacade)
    {
        this.podLookupFilter = podLookupFilter;
        this.kubernetesPodFacade = kubernetesPodFacade;
    }

    public void execute()
    {
        boolean reapedAnyPods = false;
        podLookupFilter.reset();
        while(true)
        {
            List<Pod> podsToDelete = podLookupFilter.getNextResults();
            if(podsToDelete == null || podsToDelete.size() == 0)
            {
                break;
            }
            deletePods(podsToDelete, podReapBatchSize, kubernetesPodFacade);
            reapedAnyPods = true;
        }

        if(!reapedAnyPods)
            logger.info(ReaperMessages.NO_PODS_TO_REAP.getMessage());
    }

    protected static void deletePods(List<Pod> pods, int batchSize, KubernetesPodFacade kubernetesPodFacade)
    {
        int currentIndex = 0;
        while(currentIndex < pods.size())
        {
            int lastIndex = Math.min(currentIndex + batchSize, pods.size());
            List<Pod> podBatch = pods.subList(currentIndex, lastIndex);

            try
            {
                logger.info(ReaperMessages.POD_DELETE_ATTEMPT.getMessage(podBatch.stream().map(pod -> pod.getMetadata().getName()).collect(Collectors.joining(","))));
                kubernetesPodFacade.deletePods(podBatch);
            }
            catch(Exception e)
            {
                logger.error(ReaperMessages.POD_BATCH_REAP_FAILED.getMessage(), e);
            }
            currentIndex += batchSize;
        }
    }

    public BatchedPodReaper setPodReapBatchSize(int podReapBatchSize)
    {
        this.podReapBatchSize = podReapBatchSize;
        return this;
    }
}
