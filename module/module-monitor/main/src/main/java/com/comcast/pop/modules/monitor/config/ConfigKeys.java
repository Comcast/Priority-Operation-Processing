package com.comcast.pop.modules.monitor.config;

import java.util.Set;

public interface ConfigKeys<KEY extends ConfigKey>
{
    Set<KEY> getKeys();
}
