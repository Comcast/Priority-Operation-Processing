package com.comcast.pop.modules.kube.client.config;

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

    public AliveCheckDetails setAliveCheckHost(String aliveCheckHost)
    {

        this.aliveCheckHost = aliveCheckHost;
        return this;
    }

    public String getAlivePath()
    {
        return alivePath;
    }

    public AliveCheckDetails setAlivePath(String alivePath)
    {
        this.alivePath = alivePath;
        return this;
    }

    public int getAlivePort()
    {
        return alivePort;
    }

    public AliveCheckDetails setAlivePort(int alivePort)
    {
        this.alivePort = alivePort;
        return this;
    }

    public Boolean getAliveCheckLinking()
    {
        return aliveCheckLinking;
    }

    public AliveCheckDetails setAliveCheckLinking(Boolean aliveCheckLinking)
    {
        this.aliveCheckLinking = aliveCheckLinking;
        return this;
    }

    public Integer getAliveCheckInterval()
    {
        return aliveCheckInterval;
    }

    public AliveCheckDetails setAliveCheckInterval(Integer aliveCheckInterval)
    {
        this.aliveCheckInterval = aliveCheckInterval;
        return this;
    }

    public Integer getAliveCheckFailureThreshold()
    {
        return aliveCheckFailureThreshold;
    }

    public AliveCheckDetails setAliveCheckFailureThreshold(Integer aliveCheckFailureThreshold)
    {
        this.aliveCheckFailureThreshold = aliveCheckFailureThreshold;
        return this;
    }
}
