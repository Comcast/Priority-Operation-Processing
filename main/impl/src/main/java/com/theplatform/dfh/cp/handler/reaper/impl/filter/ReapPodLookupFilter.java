package com.theplatform.dfh.cp.handler.reaper.impl.filter;

import com.theplatform.dfh.cp.handler.reaper.impl.kubernetes.KubernetesPodFacade;
import com.theplatform.dfh.cp.handler.reaper.impl.util.InstantUtil;
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
public class ReapPodLookupFilter implements PodLookupFilter
{
    private static Logger logger = LoggerFactory.getLogger(ReapPodLookupFilter.class);

    public static final int DEFAULT_POD_REAP_AGE_MINUTES = 60 * 24;

    private final String STATUS_PHASE = "status.phase";
    private List<PodPhase> podPhases;
    private String namespace = "default";
    private final KubernetesPodFacade kubernetesPodFacade;
    private int podReapAgeMinutes = 60*24; // default to a day
    private boolean lookupComplete = false;

    public ReapPodLookupFilter(KubernetesPodFacade kubernetesPodFacade)
    {
        this.kubernetesPodFacade = kubernetesPodFacade;
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

    public ReapPodLookupFilter withReapPodAgeMinutes(int podReapAgeMinutes)
    {
        this.podReapAgeMinutes = podReapAgeMinutes;
        return this;
    }

    @Override
    public List<Pod> getNextResults()
    {
        if(lookupComplete)
            return new LinkedList<>();

        List<Pod> resultPods = new LinkedList<>();
        if(podPhases != null && podPhases.size() > 0)
        {
            podPhases.forEach(p -> appendPodsByPhaseStatusAndAge(p, resultPods));
        }
        lookupComplete = true;
        return resultPods;
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
                    if(isPodPastAge(pod, podReapAgeMinutes))
                        resultPods.add(pod);
                }
            });
    }

    protected static boolean allContainerStatusesTerminated(Pod pod)
    {
        return pod.getStatus().getContainerStatuses().stream().allMatch(cs -> cs.getState() != null && cs.getState().getTerminated() != null);
    }

    protected static boolean isPodPastAge(Pod pod, int podReapAgeMinutes)
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

        return InstantUtil.haveMinutesPassedSince(finishedInstant, Instant.now(), podReapAgeMinutes);
    }

    @Override
    public void reset()
    {
        lookupComplete = false;
    }
}
