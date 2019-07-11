package com.theplatform.dfh.cp.modules.monitor.config;

public class ConfigValue<TYPE>
{
    private TYPE value;
    private ConfigKey key;

    public ConfigValue(ConfigKey key, TYPE value)
    {
        this.key = key;
        this.value = value;
    }

    public TYPE getValue()
    {
        return this.value;
    }
    public ConfigKey getKey()
    {
        return this.key;
    }
}
