package com.theplatform.dfh.cp.modules.kube.client.config;


import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fields for building a pod
 */
public class PodConfig
{
    private static final int RETRY_DELAY = 3000;


    // The prefix for the launched kubernetes pod
    private String namePrefix;

    // The docker repo path+version to the docker image to run in the pod
    private String imageName;

    // string indicating the last log line (important one)
    // that the execution of the pod needs to watch for
    private String logLineExpectation;
    private String endOfLogIdentifier;

    // If enabled the launcher will setup the endOfLogIdentifier with the logLineExpectation value
    private boolean enableOutputScraping = false;

    // arguments to pass into the docker container
    private String[] arguments;

    private Map<String, String> envVars = new HashMap<>();

    private boolean useTaintedNodes = false;

    // ServiceAccount value to execute the pod with
    private String serviceAccountName;

    // if true, a new volume (named log) is specified with an empty directory (fabric8)
    private boolean defaultEmptyDirLogging = false;

    // the max cpu request value
    private String cpuMaxRequestCount;
    // the minimum cpu request value
    private String cpuMinimumRequestCount;

    // the minimum memory amount
    private String memoryRequestCount; // todo can this be an int of long?

    // if enabled the pod follower will delete the pod at conclusion
    private Boolean reapCompletedPods;

    // Indicates whether to pull the docker image(s) each execution (specifically setting the pull policy to Always)
    private Boolean pullAlways = false;

    // The timeout for starting a pod
    private Long podScheduledTimeoutMs;

    private long podStdoutTimeout;

    private Integer podRetryCount = 1;
    private int retryDelayMilliSecs = RETRY_DELAY;

    private List<Class<? extends Throwable>> nonRetryableExceptions;
    private List<Class<? extends Throwable>> retryableExceptions;

    private ConfigMapDetails configMapDetails;
    private AliveCheckDetails aliveCheckDetails;
    private NfsDetails nfsDetails;

    public String getNamePrefix()
    {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix)
    {
        this.namePrefix = namePrefix;
    }

    public String getImageName()
    {
        return imageName;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }

    public String getLogLineExpectation()
    {
        return logLineExpectation;
    }

    public void setLogLineExpectation(String logLineExpectation)
    {
        this.logLineExpectation = logLineExpectation;
    }

    public String getEndOfLogIdentifier()
    {
        return endOfLogIdentifier;
    }

    public void setEndOfLogIdentifier(String endOfLogIdentifier)
    {
        this.endOfLogIdentifier = endOfLogIdentifier;
    }

    public boolean isEndOfLogIdentifierEmpty()
    {
        return StringUtils.isBlank(endOfLogIdentifier);
    }

    public boolean isEnableOutputScraping()
    {
        return enableOutputScraping;
    }

    public void setEnableOutputScraping(boolean enableOutputScraping)
    {
        this.enableOutputScraping = enableOutputScraping;
    }

    public String[] getArguments()
    {
        return arguments;
    }

    public void setArguments(String[] arguments)
    {
        this.arguments = arguments;
    }

    public Map<String, String> getEnvVars()
    {
        return envVars;
    }

    public void setEnvVars(Map<String, String> envVars)
    {
        this.envVars = envVars;
    }

    public void addEnvVars(String name, String value)
    {
        envVars.put(name, value);
    }

    public boolean useTaintedNodes()
    {
        return useTaintedNodes;
    }

    public void setUseTaintedNodes()
    {
        this.useTaintedNodes = useTaintedNodes;
    }

    public String getServiceAccountName()
    {
        return serviceAccountName;
    }

    public void setServiceAccountName(String serviceAccountName)
    {
        this.serviceAccountName = serviceAccountName;
    }

    public boolean hasServiceAccountName()
    {
        return serviceAccountName != null && serviceAccountName.length() > 0;
    }

    public boolean isDefaultEmptyDirLogging()
    {
        return defaultEmptyDirLogging;
    }

    public void setDefaultEmptyDirLogging(boolean defaultEmptyDirLogging)
    {
        this.defaultEmptyDirLogging = defaultEmptyDirLogging;
    }

    public String getCpuMaxRequestCount()
    {
        return cpuMaxRequestCount;
    }

    public void setCpuMaxRequestCount(String cpuMaxRequestCount)
    {
        this.cpuMaxRequestCount = cpuMaxRequestCount;
    }

    public String getCpuMinimumRequestCount()
    {
        return cpuMinimumRequestCount;
    }

    public void setCpuMinimumRequestCount(String cpuMinimumRequestCount)
    {
        this.cpuMinimumRequestCount = cpuMinimumRequestCount;
    }

    public String getMemoryRequestCount()
    {
        return memoryRequestCount;
    }

    public void setMemoryRequestCount(String memoryRequestCount)
    {
        this.memoryRequestCount = memoryRequestCount;
    }

    public Boolean getReapCompletedPods()
    {
        return reapCompletedPods;
    }

    public void setReapCompletedPods(Boolean reapCompletedPods)
    {
        this.reapCompletedPods = reapCompletedPods;
    }

    public Boolean getPullAlways()
    {
        return pullAlways;
    }

    public void setPullAlways(Boolean pullAlways)
    {
        this.pullAlways = pullAlways;
    }

    public Long getPodScheduledTimeoutMs()
    {
        return podScheduledTimeoutMs;
    }

    public void setPodScheduledTimeoutMs(Long podScheduledTimeoutMs)
    {
        this.podScheduledTimeoutMs = podScheduledTimeoutMs;
    }

    public long getPodStdoutTimeout()
    {
        return podStdoutTimeout;
    }

    public void setPodStdoutTimeout(long podStdoutTimeout)
    {
        this.podStdoutTimeout = podStdoutTimeout;
    }

    public Integer getPodRetryCount()
    {
        return podRetryCount;
    }

    public void setPodRetryCount(Integer podRetryCount)
    {
        this.podRetryCount = podRetryCount;
    }

    public int getRetryDelayMilliSecs()
    {
        return retryDelayMilliSecs;
    }

    public void setRetryDelayMilliSecs(int retryDelayMilliSecs)
    {
        this.retryDelayMilliSecs = retryDelayMilliSecs;
    }

    public List<Class<? extends Throwable>> getNonRetryableExceptions()
    {
        return nonRetryableExceptions;
    }

    public void setNonRetryableExceptions(List<Class<? extends Throwable>> nonRetryableExceptions)
    {
        this.nonRetryableExceptions = nonRetryableExceptions;
    }

    public List<Class<? extends Throwable>> getRetryableExceptions()
    {
        return retryableExceptions;
    }

    public void setRetryableExceptions(List<Class<? extends Throwable>> retryableExceptions)
    {
        this.retryableExceptions = retryableExceptions;
    }

    public boolean hasConfigMap()
    {
        return configMapDetails != null;
    }

    public ConfigMapDetails getConfigMapDetails()
    {
        return configMapDetails;
    }

    public void setConfigMapDetails(ConfigMapDetails configMapDetails)
    {
        this.configMapDetails = configMapDetails;
    }

    public AliveCheckDetails getAliveCheckDetails()
    {
        return aliveCheckDetails;
    }

    public void setAliveCheckDetails(AliveCheckDetails aliveCheckDetails)
    {
        this.aliveCheckDetails = aliveCheckDetails;
    }

    public NfsDetails getNfsDetails()
    {
        return nfsDetails;
    }

    public void setNfsDetails(NfsDetails nfsDetails)
    {
        this.nfsDetails = nfsDetails;
    }
}
