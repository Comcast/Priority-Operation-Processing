package com.theplatform.dfh.cp.modules.monitor.config;

import java.util.Set;

public interface ConfigKeys<KEY extends ConfigKey>
{
    Set<KEY> getKeys();
}
