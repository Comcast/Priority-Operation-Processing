package com.theplatform.dfh.cp.handler.reaper.impl.processor;

import com.theplatform.dfh.cp.handler.reaper.impl.filter.PodLookupFilter;
import com.theplatform.dfh.cp.handler.reaper.impl.kubernetes.KubernetesPodFacade;
import com.theplatform.dfh.cp.handler.reaper.impl.messages.ReaperMessages;
import com.theplatform.dfh.cp.handler.reaper.impl.util.InstantUtil;
import io.fabric8.kubernetes.api.model.Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pod reaper that reaps in batches from both the source and when deleting
 */
public class BatchedPodReaper
{
    private static Logger logger = LoggerFactory.getLogger(ReaperProcessor.class);
    public static final int DEFAULT_POD_REAP_BATCH_SIZE = 50;
    public static final int DEFAULT_REAPER_RUN_MAX_MINUTES = 5;

    private final PodLookupFilter podLookupFilter;
    private final KubernetesPodFacade kubernetesPodFacade;
    private int podReapBatchSize = DEFAULT_POD_REAP_BATCH_SIZE;
    private int reapRunMaxMinutes = DEFAULT_REAPER_RUN_MAX_MINUTES;
    private Instant reaperStartTime = Instant.now();

    public BatchedPodReaper(PodLookupFilter podLookupFilter, KubernetesPodFacade kubernetesPodFacade)
    {
        this.podLookupFilter = podLookupFilter;
        this.kubernetesPodFacade = kubernetesPodFacade;
    }

    public void execute()
    {
        reaperStartTime = Instant.now();

        boolean reapedAnyPods = false;
        podLookupFilter.reset();
        while(true)
        {
            List<Pod> podsToDelete = podLookupFilter.getNextResults();
            if(podsToDelete == null || podsToDelete.size() == 0)
            {
                break;
            }
            reapedAnyPods = true;
            if(!deletePods(podsToDelete, kubernetesPodFacade))
            {
                // exit immediately
                return;
            }
        }

        if(!reapedAnyPods)
            logger.info(ReaperMessages.NO_PODS_TO_REAP.getMessage());
    }

    /**
     * Deletes the specified pod list in batches as specified by the size
     * @param pods The pods to delete
     * @param kubernetesPodFacade The facade to interact with kubernetes with
     * @return true if processing should continue, false otherwise
     */
    protected boolean deletePods(List<Pod> pods, KubernetesPodFacade kubernetesPodFacade)
    {
        int currentIndex = 0;
        while(currentIndex < pods.size())
        {
            int lastIndex = Math.min(currentIndex + podReapBatchSize, pods.size());
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
            currentIndex += podReapBatchSize;

            if(InstantUtil.haveMinutesPassedSince(reaperStartTime, Instant.now(), reapRunMaxMinutes))
            {
                logger.warn("Exiting. Reap time minutes exceeded: {}", reapRunMaxMinutes);
                return false;
            }
        }
        return true;
    }

    public BatchedPodReaper setPodReapBatchSize(int podReapBatchSize)
    {
        this.podReapBatchSize = podReapBatchSize;
        return this;
    }

    public BatchedPodReaper setReapRunMaxMinutes(int reapRunMaxMinutes)
    {
        this.reapRunMaxMinutes = reapRunMaxMinutes;
        return this;
    }
}
