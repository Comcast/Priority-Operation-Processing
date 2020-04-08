package com.comcast.pop.modules.monitor.metric;

import com.comcast.pop.modules.monitor.config.ConfigKey;
import com.comcast.pop.modules.monitor.config.ConfigKeys;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LoggingConfigKeys implements ConfigKeys<ConfigKey>
{
    public static final ConfigKey<Boolean> ENABLED = new ConfigKey<>("logging.metric.enabled", true, Boolean.class);
    public static final ConfigKey<Integer> REPORT_FREQUENCY = new ConfigKey<>("logging.metric.report.frequency", 120000, Integer.class);

    public static final Set<ConfigKey> keys = new HashSet<>(Arrays.asList(
        REPORT_FREQUENCY,
        ENABLED
    ));

    @Override
    public Set<ConfigKey> getKeys()
    {
        return keys;
    }
}
