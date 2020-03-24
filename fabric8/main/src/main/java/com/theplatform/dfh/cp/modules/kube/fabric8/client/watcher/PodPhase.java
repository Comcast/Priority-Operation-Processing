package com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher;

import io.fabric8.kubernetes.api.model.PodStatus;

/**
 *
 */
public enum PodPhase
{
    // PodPhase names are in all-caps to work with fromPodStatus method
    PENDING("Pending"),
    RUNNING("Running"),
    SUCCEEDED("Succeeded"),
    FAILED("Failed"),
    UNKNOWN("Unknown");

    private String label;

    PodPhase(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public boolean hasStarted()
    {
        return (this != PodPhase.PENDING && this != PodPhase.UNKNOWN);
    }

    public boolean hasFinished()
    {
        return (this == PodPhase.SUCCEEDED || this == PodPhase.FAILED);
    }

    public boolean isFailed()
    {
        return this == PodPhase.FAILED;
    }

    public static PodPhase fromPodStatus(PodStatus podStatus)
    {
        if (podStatus == null || podStatus.getPhase() == null)
            return PodPhase.UNKNOWN;

        try {
            return PodPhase.valueOf(podStatus.getPhase().toUpperCase());
        } catch (IllegalArgumentException e)
        {
            return PodPhase.UNKNOWN;
        }
    }
}
