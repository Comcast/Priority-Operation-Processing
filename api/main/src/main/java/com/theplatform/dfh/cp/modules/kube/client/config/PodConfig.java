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

                                        //  Sec  MILLI  Min
    private long DEFAULT_SCHEDULED_TIMEOUT = 60 * 1000 * 5;
                                                   //  Sec  MILLI  Min
    private static final long DEFAULT_STDOUT_TIMEOUT = 60 * 1000 * 2;


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
    private Long podScheduledTimeoutMs = DEFAULT_SCHEDULED_TIMEOUT;

    private Long podStdoutTimeout = DEFAULT_STDOUT_TIMEOUT;

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

    public PodConfig setNamePrefix(String namePrefix)
    {
        this.namePrefix = namePrefix;
        return this;
    }

    public String getImageName()
    {
        return imageName;
    }

    public PodConfig setImageName(String imageName)
    {
        this.imageName = imageName;
        return this;
    }

    public String getLogLineExpectation()
    {
        return logLineExpectation;
    }

    public PodConfig setLogLineExpectation(String logLineExpectation)
    {
        this.logLineExpectation = logLineExpectation;
        return this;
    }

    public String getEndOfLogIdentifier()
    {
        return endOfLogIdentifier;
    }

    public PodConfig setEndOfLogIdentifier(String endOfLogIdentifier)
    {
        this.endOfLogIdentifier = endOfLogIdentifier;
        return this;
    }

    public boolean isEndOfLogIdentifierEmpty()
    {
        return StringUtils.isBlank(endOfLogIdentifier);
    }

    public boolean isEnableOutputScraping()
    {
        return enableOutputScraping;
    }

    public PodConfig setEnableOutputScraping(boolean enableOutputScraping)
    {
        this.enableOutputScraping = enableOutputScraping;
        return this;
    }

    public String[] getArguments()
    {
        return arguments;
    }

    public PodConfig setArguments(String[] arguments)
    {
        this.arguments = arguments;
        return this;
    }

    public Map<String, String> getEnvVars()
    {
        return envVars;
    }

    public PodConfig setEnvVars(Map<String, String> envVars)
    {
        this.envVars = envVars;
        return this;
    }

    public PodConfig addEnvVars(String name, String value)
    {
        envVars.put(name, value);
        return this;
    }

    public boolean useTaintedNodes()
    {
        return useTaintedNodes;
    }

    public PodConfig setUseTaintedNodes()
    {
        this.useTaintedNodes = useTaintedNodes;
        return this;
    }

    public String getServiceAccountName()
    {
        return serviceAccountName;
    }

    public PodConfig setServiceAccountName(String serviceAccountName)
    {
        this.serviceAccountName = serviceAccountName;
        return this;
    }

    public boolean hasServiceAccountName()
    {
        return serviceAccountName != null && serviceAccountName.length() > 0;
    }

    public boolean isDefaultEmptyDirLogging()
    {
        return defaultEmptyDirLogging;
    }

    public PodConfig setDefaultEmptyDirLogging(boolean defaultEmptyDirLogging)
    {
        this.defaultEmptyDirLogging = defaultEmptyDirLogging;
        return this;
    }

    public String getCpuMaxRequestCount()
    {
        return cpuMaxRequestCount;
    }

    public PodConfig setCpuMaxRequestCount(String cpuMaxRequestCount)
    {
        this.cpuMaxRequestCount = cpuMaxRequestCount;
        return this;
    }

    public String getCpuMinimumRequestCount()
    {
        return cpuMinimumRequestCount;
    }

    public PodConfig setCpuMinimumRequestCount(String cpuMinimumRequestCount)
    {
        this.cpuMinimumRequestCount = cpuMinimumRequestCount;
        return this;
    }

    public String getMemoryRequestCount()
    {
        return memoryRequestCount;
    }

    public PodConfig setMemoryRequestCount(String memoryRequestCount)
    {
        this.memoryRequestCount = memoryRequestCount;
        return this;
    }

    public Boolean getReapCompletedPods()
    {
        return reapCompletedPods;
    }

    public PodConfig setReapCompletedPods(Boolean reapCompletedPods)
    {
        this.reapCompletedPods = reapCompletedPods;
        return this;
    }

    public Boolean getPullAlways()
    {
        return pullAlways;
    }

    public PodConfig setPullAlways(Boolean pullAlways)
    {
        this.pullAlways = pullAlways;
        return this;
    }

    public Long getPodScheduledTimeoutMs()
    {
        return podScheduledTimeoutMs;
    }

    public PodConfig setPodScheduledTimeoutMs(Long podScheduledTimeoutMs)
    {
        this.podScheduledTimeoutMs = podScheduledTimeoutMs;
        return this;
    }

    public long getPodStdoutTimeout()
    {
        return podStdoutTimeout;
    }

    public PodConfig setPodStdoutTimeout(long podStdoutTimeout)
    {
        this.podStdoutTimeout = podStdoutTimeout;
        return this;
    }

    public Integer getPodRetryCount()
    {
        return podRetryCount;
    }

    public PodConfig setPodRetryCount(Integer podRetryCount)
    {
        this.podRetryCount = podRetryCount;
        return this;
    }

    public int getRetryDelayMilliSecs()
    {
        return retryDelayMilliSecs;
    }

    public PodConfig setRetryDelayMilliSecs(int retryDelayMilliSecs)
    {
        this.retryDelayMilliSecs = retryDelayMilliSecs;
        return this;
    }

    public List<Class<? extends Throwable>> getNonRetryableExceptions()
    {
        return nonRetryableExceptions;
    }

    public PodConfig setNonRetryableExceptions(List<Class<? extends Throwable>> nonRetryableExceptions)
    {
        this.nonRetryableExceptions = nonRetryableExceptions;
        return this;
    }

    public List<Class<? extends Throwable>> getRetryableExceptions()
    {
        return retryableExceptions;
    }

    public PodConfig setRetryableExceptions(List<Class<? extends Throwable>> retryableExceptions)
    {
        this.retryableExceptions = retryableExceptions;
        return this;
    }

    public boolean hasConfigMap()
    {
        return configMapDetails != null;
    }

    public ConfigMapDetails getConfigMapDetails()
    {
        return configMapDetails;
    }

    public PodConfig setConfigMapDetails(ConfigMapDetails configMapDetails)
    {
        this.configMapDetails = configMapDetails;
        return this;
    }

    public AliveCheckDetails getAliveCheckDetails()
    {
        return aliveCheckDetails;
    }

    public PodConfig setAliveCheckDetails(AliveCheckDetails aliveCheckDetails)
    {
        this.aliveCheckDetails = aliveCheckDetails;
        return this;
    }

    public NfsDetails getNfsDetails()
    {
        return nfsDetails;
    }

    public PodConfig setNfsDetails(NfsDetails nfsDetails)
    {
        this.nfsDetails = nfsDetails;
        return this;
    }
}
