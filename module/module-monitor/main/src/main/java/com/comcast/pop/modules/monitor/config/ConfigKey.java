package com.comcast.pop.modules.monitor.config;

import com.comcast.pop.modules.monitor.config.converter.DefaultPropertyConverter;
import com.comcast.pop.modules.monitor.config.converter.ConfigPropertyConverter;

public class ConfigKey<T>
{
    private final String propertyKey;
    private final T defaultValue;
    private final Class<T> type;
    private final ConfigPropertyConverter<T> configPropertyConverter;

    public ConfigKey(String propertyKey, T defaultValue, Class<T> type)
    {
        this(propertyKey, defaultValue, type, new DefaultPropertyConverter<>(type));
    }

    public ConfigKey(String propertyKey, T defaultValue, Class<T> type, ConfigPropertyConverter<T> configPropertyConverter)
    {
        this.propertyKey = propertyKey;
        this.defaultValue = defaultValue;
        this.type = type;
        this.configPropertyConverter = configPropertyConverter;
    }

    public String getPropertyKey()
    {
        return propertyKey;
    }

    public T getDefaultValue()
    {
        return defaultValue;
    }

    public Class<T> getType()
    {
        return type;
    }

    public ConfigPropertyConverter<T> getConfigPropertyConverter()
    {
        return configPropertyConverter;
    }

    @Override
    public String toString()
    {
        return getPropertyKey();
    }

    @Override
    public boolean equals(Object o)
    {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof ConfigKey)) {
            return false;
        }
        ConfigKey c = (ConfigKey) o;

        if (propertyKey != null
                ? !propertyKey.equals(c.propertyKey)
                : c.propertyKey != null)
            return false;
        if (defaultValue != null
                ? !defaultValue.equals(c.defaultValue)
                : c.defaultValue != null)
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        int result = 31 * (propertyKey != null
                ? propertyKey.hashCode()
                : 0);
        result = 31 * result + (defaultValue != null
                ? defaultValue.hashCode()
                : 0);
        return result;
    }
}
