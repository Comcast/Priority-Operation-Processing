package com.theplatform.dfh.cp.modules.monitor.alive;

import com.theplatform.dfh.cp.modules.monitor.config.ConfigKey;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigKeys;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AliveCheckConfigKeys implements ConfigKeys<ConfigKey>
{
    public static final ConfigKey<Integer> CHECK_FREQUENCY = new ConfigKey<>("alive.check.frequency", 10000, Integer.class);
    public static final ConfigKey<Boolean> ENABLED = new ConfigKey<>("alive.check.enabled", true, Boolean.class);

    public static final Set<ConfigKey> keys = new HashSet<>(Arrays.asList(
        CHECK_FREQUENCY,
        ENABLED
    ));

    @Override
    public Set<ConfigKey> getKeys()
    {
        return keys;
    }
}
