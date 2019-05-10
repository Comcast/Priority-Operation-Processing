package com.theplatform.dfh.cp.handler.reaper.impl.filter;

import com.theplatform.com.dfh.modules.sync.util.InstantUtil;
import com.theplatform.com.dfh.modules.sync.util.Producer;
import com.theplatform.com.dfh.modules.sync.util.ProducerResult;
import com.theplatform.dfh.cp.handler.reaper.impl.kubernetes.KubernetesPodFacade;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodPhase;
import io.fabric8.kubernetes.api.model.Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * LookupFilter for pods to reap (a combination of age and status)
 *
 * Due to the limitations of our kube client this does not perform pagination (yet).
 *
 * All pods for our handlers/components only use a single container. This code ASSUMES a single container per pod.
 */
public class ReapPodLookupFilter implements Producer<Pod>
{
    public static final int DEFAULT_POD_REAP_AGE_MINUTES = 60*24;
    private static Logger logger = LoggerFactory.getLogger(ReapPodLookupFilter.class);
    private static final String STATUS_PHASE = "status.phase";

    private final KubernetesPodFacade kubernetesPodFacade;
    private final Instant reapUpperBoundUTC;
    private List<PodPhase> podPhases;
    private String namespace = "default";
    private boolean lookupComplete = false;

    public ReapPodLookupFilter(KubernetesPodFacade kubernetesPodFacade, Instant reapUpperBoundUTC)
    {
        this.kubernetesPodFacade = kubernetesPodFacade;
        this.reapUpperBoundUTC = reapUpperBoundUTC;
    }

    public ReapPodLookupFilter withPodPhases(PodPhase... podPhases)
    {
        this.podPhases = new ArrayList<>(Arrays.asList(podPhases));
        return this;
    }

    public ReapPodLookupFilter withNamespace(String namespace)
    {
        this.namespace = namespace;
        return this;
    }

    @Override
    public ProducerResult<Pod> produce(Instant processEndTime)
    {
        if(lookupComplete)
            return new ProducerResult<>();

        // TODO: There is no support for pagination for kubernetes yet (need a newer client)
        List<Pod> resultPods = new LinkedList<>();
        if(podPhases != null && podPhases.size() > 0)
        {
            podPhases.forEach(p -> appendPodsByPhaseStatusAndAge(p, resultPods));
        }
        lookupComplete = true;
        return new ProducerResult<Pod>().setItemsProduced(resultPods);
    }

    protected void appendPodsByPhaseStatusAndAge(PodPhase podPhase, List<Pod> resultPods)
    {
        List<Pod> pods = kubernetesPodFacade.lookupPods(namespace, Collections.singletonMap(STATUS_PHASE, podPhase.getLabel()));
        pods.stream()
            .filter(ReapPodLookupFilter::allContainerStatusesTerminated)
            .forEach(pod ->
            {
                FinalPodPhaseInfo podPhaseInfo = FinalPodPhaseInfo.fromPodStatus(pod.getMetadata().getName(), pod.getStatus());
                if(podPhaseInfo.phase.hasFinished())
                {
                    if(isPodPastAge(pod, reapUpperBoundUTC))
                        resultPods.add(pod);
                }
            });
    }

    protected static boolean allContainerStatusesTerminated(Pod pod)
    {
        return pod.getStatus().getContainerStatuses().stream().allMatch(cs -> cs.getState() != null && cs.getState().getTerminated() != null);
    }

    protected static boolean isPodPastAge(Pod pod, Instant reapUpperBoundUTC)
    {
        String finishedAtString;
        if(pod.getStatus().getContainerStatuses() != null && pod.getStatus().getContainerStatuses().size() > 0)
        {
            // normal operation completion (pod started and exited)
            // the end time of the 0 index container within the pod is acceptable for our use
            finishedAtString = pod.getStatus().getContainerStatuses().get(0).getState().getTerminated().getFinishedAt();
        }
        else
        {
            // abnormal operation, we only have the start time on a failed pod (might have run out of CPU or was never scheduled)
            logger.warn("Abnormal pod exit detected. Using start time for reap evaluation. Pod={} ExitReason={}", pod.getMetadata().getName(), pod.getStatus().getReason());
            finishedAtString = pod.getStatus().getStartTime();
        }

        Instant finishedInstant;
        try
        {
            finishedInstant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(finishedAtString));
        }
        catch(DateTimeParseException e)
        {
            logger.error("Failed to parse the finished time [{}]. Skipping reap for pod={}", finishedAtString, pod.getMetadata().getName());
            return false;
        }

        return InstantUtil.isAfterOrEqual(reapUpperBoundUTC, finishedInstant);
    }

    @Override
    public void reset()
    {
        lookupComplete = false;
    }

    @Override
    public String toString()
    {
        return "ReapPodLookupFilter{" +
            "kubernetesPodFacade=" + kubernetesPodFacade +
            ", reapUpperBoundUTC=" + reapUpperBoundUTC +
            ", podPhases=" + podPhases +
            ", namespace='" + namespace + '\'' +
            ", lookupComplete=" + lookupComplete +
            '}';
    }
}
