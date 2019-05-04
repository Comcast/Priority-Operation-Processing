package com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.PodStatus;

import java.util.Optional;

/**
 * Details the final status of the pod (Succeeded or Failed) and the exitCode
 */
public class FinalPodPhaseInfo
{
    private static final int DEFAULT_EXIT_CODE = 0;

    public final PodPhase phase;
    public final String name;        // todo can we remove this?
    public final String reason;
    public final Integer exitCode;

    public FinalPodPhaseInfo(String podName, PodPhase podPhase, int exitCode, String reason)
    {
        this.name = podName;
        this.phase = podPhase;
        this.exitCode = exitCode;
        this.reason = reason;
    }

    // Note: that this only works for single docker image pods
    public static FinalPodPhaseInfo fromPodStatus(String podName, PodStatus status)
    {
        int exitCode = DEFAULT_EXIT_CODE;

        Optional<ContainerStatus> containerStatus = status.getContainerStatuses().stream()
            .filter(cs -> cs.getState() != null && cs.getState().getTerminated() != null)
            .findFirst();

        // normal operation completion
        if(containerStatus.isPresent())
            exitCode = containerStatus.get().getState().getTerminated().getExitCode();
        // else (nothing special, likely a fail -- observed out of cpu)

        return new FinalPodPhaseInfo(podName, PodPhase.fromPodStatus(status), exitCode, status.getReason());
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
