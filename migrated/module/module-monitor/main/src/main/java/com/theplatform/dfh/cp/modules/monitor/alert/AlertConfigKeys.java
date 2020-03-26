package com.theplatform.dfh.cp.modules.monitor.alert;

import com.theplatform.dfh.cp.modules.monitor.config.ConfigKey;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigKeys;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AlertConfigKeys implements ConfigKeys<ConfigKey>
{
    public static final ConfigKey<Integer> THRESHOLD = new ConfigKey<>("alert.count.threshold", 2, Integer.class);
    public static final ConfigKey<Integer> RETRY_TIMEOUT = new ConfigKey<>("alert.retry.timeout", 200, Integer.class);
    public static final ConfigKey<Integer> RETRY_COUNT = new ConfigKey<>("alert.retry.count", 3, Integer.class);
    public static final ConfigKey<String> HOST = new ConfigKey<>("alert.host", null, String.class);
    public static final ConfigKey<String> LEVEL_FAILED = new ConfigKey<>("alert.level.failed", AlertLevel.CRITICAL.name(), String.class);
    public static final ConfigKey<String> LEVEL_PASSED = new ConfigKey<>("alert.level.passed", AlertLevel.CLEAR.name(), String.class);
    public static final ConfigKey<Boolean> ENABLED = new ConfigKey<>("alert.enabled", true, Boolean.class);

    public static final Set<ConfigKey> keys = new HashSet<>(Arrays.asList(
        THRESHOLD,
        HOST,
        RETRY_COUNT,
        RETRY_TIMEOUT,
        LEVEL_FAILED,
        LEVEL_PASSED
    ));

    @Override
    public Set<ConfigKey> getKeys()
    {
        return keys;
    }
}
