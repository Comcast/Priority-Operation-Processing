package com.theplatform.dfh.cp.kube.client.config;

/**
 *
 */
public class AliveCheckDetails {
    private final int ALIVECHECK_INTERVAL = 2;

    private String aliveCheckHost;
    private String alivePath;
    private int alivePort;
    // Enables alive checking within the pod (as defined in a container spec)
    private Boolean aliveCheckLinking;
    private Integer aliveCheckInterval = ALIVECHECK_INTERVAL;
    private Integer aliveCheckFailureThreshold = 10;

    public String getAliveCheckHost()
    {
        return aliveCheckHost;
    }

    public void setAliveCheckHost(String aliveCheckHost)
    {
        this.aliveCheckHost = aliveCheckHost;
    }

    public String getAlivePath()
    {
        return alivePath;
    }

    public void setAlivePath(String alivePath)
    {
        this.alivePath = alivePath;
    }

    public int getAlivePort()
    {
        return alivePort;
    }

    public void setAlivePort(int alivePort)
    {
        this.alivePort = alivePort;
    }

    public Boolean getAliveCheckLinking()
    {
        return aliveCheckLinking;
    }

    public void setAliveCheckLinking(Boolean aliveCheckLinking)
    {
        this.aliveCheckLinking = aliveCheckLinking;
    }

    public Integer getAliveCheckInterval()
    {
        return aliveCheckInterval;
    }

    public void setAliveCheckInterval(Integer aliveCheckInterval)
    {
        this.aliveCheckInterval = aliveCheckInterval;
    }

    public Integer getAliveCheckFailureThreshold()
    {
        return aliveCheckFailureThreshold;
    }

    public void setAliveCheckFailureThreshold(Integer aliveCheckFailureThreshold)
    {
        this.aliveCheckFailureThreshold = aliveCheckFailureThreshold;
    }
}
