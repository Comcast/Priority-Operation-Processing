package com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher;

import io.fabric8.kubernetes.api.model.PodStatus;

/**
 * Details the final status of the pod (Succeeded or Failed) and the exitCode
 */
public class FinalPodPhaseInfo
{
    public final PodPhase phase;
    public final String name;        // todo can we remove this?
    public final int exitCode;

    public FinalPodPhaseInfo(String podName, PodPhase podPhase, int exitCode)
    {
        this.name = podName;
        this.phase = podPhase;
        this.exitCode = exitCode;
    }

    // Note: that this only works for single docker image pods
    public static FinalPodPhaseInfo fromPodStatus(String podName, PodStatus status)
    {
        int exitCode = status.getContainerStatuses().stream()
            .filter(cs -> cs.getState() != null && cs.getState().getTerminated() != null)
            .findFirst().get().getState().getTerminated().getExitCode();

        FinalPodPhaseInfo finalPodPhaseInfo = new FinalPodPhaseInfo(podName, PodPhase.fromPodStatus(status), exitCode);
        return finalPodPhaseInfo;
    }

    @Override
    public String toString()
    {
        return "LastPhase{" +
            "phase=" + phase +
            ", name='" + name + '\'' +
            ", exitCode=" + exitCode +
            '}';
    }
}
