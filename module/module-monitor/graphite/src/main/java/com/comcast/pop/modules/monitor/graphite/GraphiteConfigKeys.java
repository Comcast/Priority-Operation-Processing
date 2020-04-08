package com.comcast.pop.modules.monitor.graphite;

import com.comcast.pop.modules.monitor.config.ConfigKey;
import com.comcast.pop.modules.monitor.config.ConfigKeys;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GraphiteConfigKeys implements ConfigKeys<ConfigKey>
{
    public static final ConfigKey<Boolean> ENABLED = new ConfigKey<>("graphite.enabled", true, Boolean.class);
    public static final ConfigKey<String> PATH = new ConfigKey<>("graphite.path", null, String.class);
    public static final ConfigKey<String> ENDPOINT = new ConfigKey<>("graphite.endpoint", null, String.class);
    public static final ConfigKey<Integer> PORT = new ConfigKey<>("graphite.port", 2003, Integer.class);
    public static final ConfigKey<Integer> REPORT_FREQUENCY = new ConfigKey<>("graphite.report.frequency", 300000, Integer.class);

    public static final Set<ConfigKey> keys = new HashSet<>(Arrays.asList(
        ENABLED,
        PATH,
        ENDPOINT,
        PORT,
        REPORT_FREQUENCY
    ));


    @Override
    public Set<ConfigKey> getKeys()
    {
        return keys;
    }
}

