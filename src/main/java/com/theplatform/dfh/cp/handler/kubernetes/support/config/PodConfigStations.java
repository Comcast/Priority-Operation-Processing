package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.handler.base.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.api.NamedField;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;

import java.util.Optional;

/**
 * Each enum is a 'station' that:
 * - defines a DFH-Fission supported configuration key;
 * - defines a default value; and
 * - provides an operation to retrieve a configured value and set it on a PodConfig instance.
 */
public enum PodConfigStations implements NamedField, ConfigurePod
{
    eolIdentifier("cp.kubernetes.podconfig.endOfLogIdentifier")
            {
                @Override
                public void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever)
                {
                    podConfig.setEndOfLogIdentifier(fieldRetriever.map(fieldRetriever1 -> fieldRetriever1.getField(getFieldName(),DEFAULT_STRING)).orElse(DEFAULT_STRING));
                }
            },
    namePrefix("cp.kubernetes.podconfig.namePrefix")
            {
                @Override
                public void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever)
                {
                    podConfig.setNamePrefix(fieldRetriever.map(fieldRetriever1 -> fieldRetriever1.getField(getFieldName(),DEFAULT_STRING)).orElse(DEFAULT_STRING));
                }
            },
    imageName("cp.kubernetes.podconfig.docker.imageName")
            {
                @Override
                public void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever)
                {
                    podConfig.setImageName(fieldRetriever.map(fieldRetriever1 -> fieldRetriever1.getField(getFieldName(), DEFAULT_STRING)).orElse(DEFAULT_STRING));
                }
            },
    dockerImagePullAlways("cp.kubernetes.podconfig.docker.imagePullAlways")
            {
                private final Boolean DEFAULT_PULL_ALWAYS = false;

                @Override
                public void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever)
                {
                    podConfig.setPullAlways(fieldRetriever.map(fieldRetriever1 -> fieldRetriever1.getBoolean(getFieldName(), DEFAULT_PULL_ALWAYS)).orElse(DEFAULT_PULL_ALWAYS));
                }
            },

    reapCompletedPods("cp.kubernetes.podconfig.reapCompletedPods")
            {
                private final Boolean DEFAULT_REAP_COMPLETED = false;
                @Override
                public void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever)
                {
                    podConfig.setReapCompletedPods(fieldRetriever.map(fieldRetriever1 -> fieldRetriever1.getBoolean(getFieldName(), DEFAULT_REAP_COMPLETED)).orElse(DEFAULT_REAP_COMPLETED));
                }
            },
    useTaintedNodes("cp.kubernetes.podconfig.useTaintedNodes")
            {
                private final Boolean DEFAULT_USE_TAINTED_NODES = false;

                @Override
                public void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever)
                {
                    podConfig.setUseTaintedNodes(fieldRetriever.map(fieldRetriever1 ->  fieldRetriever1.getBoolean(getFieldName(), DEFAULT_USE_TAINTED_NODES)).orElse(DEFAULT_USE_TAINTED_NODES));
                }
            },
    useTaintedNodesSelector("cp.kubernetes.podconfig.taintedSelector")
            {
                @Override
                public void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever)
                {
                    podConfig.setTaintedNodeSelector(fieldRetriever.map(fieldRetriever1 -> fieldRetriever1.getField(getFieldName(), DEFAULT_STRING)).orElse(DEFAULT_STRING));
                }
            },
    useTaintedNodesToleration("cp.kubernetes.podconfig.taintedToleration")
            {
                @Override
                public void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever)
                {
                    podConfig.setTaintedNodeToleration(fieldRetriever.map(fieldRetriever1 -> fieldRetriever1.getField(getFieldName(), DEFAULT_STRING)).orElse(DEFAULT_STRING));
                }
            },
    podTimeoutScheduledMS("cp.kubernetes.podconfig.timeout.scheduledMs")
            {
                private long DEFAULT_SCHEDULED_TIMEOUT = 300000L;
                @Override
                public void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever)
                {
                    podConfig.setPodScheduledTimeoutMs(fieldRetriever.map(fieldRetriever1 ->  fieldRetriever1.getLong(getFieldName(), DEFAULT_SCHEDULED_TIMEOUT)).orElse(DEFAULT_SCHEDULED_TIMEOUT));
                }
            },
    podTimeoutStdOut("cp.kubernetes.podconfig.timeout.stdout")
            {
                private final long DEFAULT_STDOUT_TIMEOUT = 120000L;
                @Override
                public void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever)
                {
                    podConfig.setPodStdoutTimeout(fieldRetriever.map(fieldRetriever1 ->  fieldRetriever1.getLong(getFieldName(), DEFAULT_STDOUT_TIMEOUT)).orElse(DEFAULT_STDOUT_TIMEOUT));
                }
            },
    retryCount("cp.kubernetes.podconfig.retry.count")
            {
                private final int DEFAULT_RETRY_COUNT = 0;
                @Override
                public void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever)
                {
                    podConfig.setPodRetryCount(fieldRetriever.map(fieldRetriever1 ->  fieldRetriever1.getInt(getFieldName(), DEFAULT_RETRY_COUNT)).orElse(DEFAULT_RETRY_COUNT));
                }
            },
    retryDelay("cp.kubernetes.podconfig.retry.delay")
            {
                private final int DEFAULT_RETRY_DELAY = 3000;
                @Override
                public void setPodConfig(PodConfig podConfig, Optional<FieldRetriever> fieldRetriever)
                {
                    podConfig.setRetryDelayMilliSecs(fieldRetriever.map(fieldRetriever1 ->  fieldRetriever1.getInt(getFieldName(), DEFAULT_RETRY_DELAY)).orElse(DEFAULT_RETRY_DELAY));
                }
            };


    private static final String DEFAULT_STRING = null;

    private final String fieldName;

    PodConfigStations(String key)
    {
        this.fieldName = key;
    }

    public String getFieldName()
    {
        return fieldName;
    }
}
