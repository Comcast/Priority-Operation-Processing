package com.theplatform.dfh.cp.handler.reaper.impl.filter;

import com.theplatform.dfh.cp.handler.reaper.impl.kubernetes.KubernetesPodFacade;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.FinalPodPhaseInfo;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.PodPhase;
import io.fabric8.kubernetes.api.model.Pod;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ReapPodLookupFilter implements PodLookupFilter
{
    public static final int DEFAULT_POD_REAP_AGE_MINUTES = 60 * 24;

    private final String STATUS_PHASE = "status.phase";
    private List<PodPhase> podPhases;
    private String namespace = "default";
    private final KubernetesPodFacade kubernetesPodFacade;
    private int podReapAgeMinutes = 60*24; // default to a day

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
    public List<Pod> performLookup()
    {
        List<Pod> resultPods = new LinkedList<>();
        if(podPhases != null && podPhases.size() > 0)
        {
            podPhases.forEach(p -> appendPodsByPhaseStatus(p, resultPods));
        }
        return resultPods;
    }

    protected void appendPodsByPhaseStatus(PodPhase podPhase, List<Pod> resultPods)
    {
        List<Pod> pods = kubernetesPodFacade.lookupPods(namespace, Collections.singletonMap(STATUS_PHASE, podPhase.getLabel()));
        pods.stream()
            .filter(pod -> pod.getStatus().getContainerStatuses().stream().allMatch(cs -> cs.getState() != null && cs.getState().getTerminated() != null))
            .forEach(pod ->
            {
                FinalPodPhaseInfo podPhaseInfo = FinalPodPhaseInfo.fromPodStatus(pod.getMetadata().getName(), pod.getStatus());
                if(podPhaseInfo.phase.hasFinished())
                {
                    // example: 2019-04-23T02:43:09Z (ISO_INSTANT)
                    String finishedAtString = pod.getStatus().getContainerStatuses().get(0).getState().getTerminated().getFinishedAt();
                    Instant.now();
                    Instant instant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(finishedAtString));
                    if(!Duration.between(instant, Instant.now()).minusMinutes(podReapAgeMinutes).isNegative())
                    {
                        resultPods.add(pod);
                    }
                }
            });
    }
}
