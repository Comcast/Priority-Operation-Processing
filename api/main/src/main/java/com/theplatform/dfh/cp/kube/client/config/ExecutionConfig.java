package com.theplatform.dfh.cp.kube.client.config;

import com.theplatform.dfh.cp.kube.client.LogLineAccumulator;
import com.theplatform.dfh.cp.kube.client.CpuRequestModulator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *  Details for Pod creation
 */
public class ExecutionConfig
{
    // labels for the pod
    private String externalId = UUID.randomUUID().toString();
    private String externalGroupId = UUID.randomUUID().toString();   // todo where is this used

    private String name;    // todo have Launcher set this as the podConfig prefix + a random UUID

    private Map<String, String> envVars = new HashMap<>();

    // these are values on the PodConfig that can be overriden
    private String memoryRequestCount;

    // builds a String adhering to the requirements of fabric8 client request for cpu
    private CpuRequestModulator cpuRequestModulator;        // todo should this be on the PodConfig?

    private LogLineAccumulator logLineAccumulator;

    public String getExternalId()
    {
        return externalId;
    }

    public void setExternalId(String externalId)
    {
        this.externalId = externalId;
    }

    public String getExternalGroupId()
    {
        return externalGroupId;
    }

    public void setExternalGroupId(String externalGroupId)
    {
        this.externalGroupId = externalGroupId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<String, String> getEnvVars()
    {
        return envVars;
    }

    public void setEnvVars(Map<String, String> envVars)
    {
        this.envVars = envVars;
    }

    public void addEnvVar(String name, String value)
    {
        envVars.put(name, value);
    }

    public boolean hasEnvVars()
    {
        return envVars == null || envVars.isEmpty();
    }

    public String getMemoryRequestCount()
    {
        return memoryRequestCount;
    }

    public void setMemoryRequestCount(String memoryRequestCount)
    {
        this.memoryRequestCount = memoryRequestCount;
    }

    public CpuRequestModulator getCpuRequestModulator()
    {
        return cpuRequestModulator;
    }

    public void setCpuRequestModulator(CpuRequestModulator cpuRequestModulator)
    {
        this.cpuRequestModulator = cpuRequestModulator;
    }

    public LogLineAccumulator getLogLineAccumulator()
    {
        return logLineAccumulator;
    }

    public void setLogLineAccumulator(LogLineAccumulator logLineAccumulator)
    {
        this.logLineAccumulator = logLineAccumulator;
    }

    // todo getter for the payload in an implementation of this config: HandlerExecutionConfig?


}
