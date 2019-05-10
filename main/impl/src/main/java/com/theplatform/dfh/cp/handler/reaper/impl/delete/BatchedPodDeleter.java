package com.theplatform.dfh.cp.handler.reaper.impl.delete;

import com.theplatform.com.dfh.modules.sync.util.Consumer;
import com.theplatform.com.dfh.modules.sync.util.ConsumerResult;
import com.theplatform.com.dfh.modules.sync.util.InstantUtil;
import com.theplatform.dfh.cp.handler.reaper.impl.kubernetes.KubernetesPodFacade;
import com.theplatform.dfh.cp.handler.reaper.impl.messages.ReaperMessages;
import io.fabric8.kubernetes.api.model.Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BatchedPodDeleter implements Consumer<Pod>
{
    private static Logger logger = LoggerFactory.getLogger(BatchedPodDeleter.class);

    public static final int DEFAULT_POD_REAP_BATCH_SIZE = 50;
    private final KubernetesPodFacade kubernetesPodFacade;
    private int podReapBatchSize = DEFAULT_POD_REAP_BATCH_SIZE;

    public BatchedPodDeleter(KubernetesPodFacade kubernetesPodFacade)
    {
        this.kubernetesPodFacade = kubernetesPodFacade;
    }

    @Override
    public ConsumerResult<Pod> consume(Collection<Pod> collection, Instant endProcessingTime)
    {
        if(collection == null)
        {
            logger.info("Input collection is null. No deletes to perform.");
            return new ConsumerResult<>();
        }

        logger.info("Attempting to delete {} pods", collection.size());

        int currentIndex = 0;
        List<Pod> podsToDelete = new LinkedList<>(collection);
        while(currentIndex < podsToDelete.size())
        {
            int lastIndex = Math.min(currentIndex + podReapBatchSize, podsToDelete.size());
            List<Pod> deleteBatch = podsToDelete.subList(currentIndex, lastIndex);
            deletePods(deleteBatch);
            currentIndex += podReapBatchSize;

            if(InstantUtil.isNowAfterOrEqual(endProcessingTime))
                break;
        }
        logger.info("Deleted {} pods", podsToDelete.size());
        return new ConsumerResult<Pod>().setItemsConsumedCount(podsToDelete.size());
    }

    /**
     * Deletes the specified pod list in batches as specified by the size
     * @param pods The pods to delete
     * @return true if processing should continue, false otherwise
     */
    protected void deletePods(List<Pod> pods)
    {
        try
        {
            logger.info(ReaperMessages.POD_DELETE_ATTEMPT.getMessage(pods.stream().map(pod -> pod.getMetadata().getName()).collect(Collectors.joining(","))));
            kubernetesPodFacade.deletePods(pods);
        }
        catch(Exception e)
        {
            logger.error(ReaperMessages.POD_BATCH_REAP_FAILED.getMessage(), e);
        }
    }

    public BatchedPodDeleter setPodReapBatchSize(int podReapBatchSize)
    {
        this.podReapBatchSize = podReapBatchSize;
        return this;
    }

    @Override
    public String toString()
    {
        return "BatchedPodDeleter{" +
            "kubernetesPodFacade=" + kubernetesPodFacade +
            ", podReapBatchSize=" + podReapBatchSize +
            '}';
    }
}
