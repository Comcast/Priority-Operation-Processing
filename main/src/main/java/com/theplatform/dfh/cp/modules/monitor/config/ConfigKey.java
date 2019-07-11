package com.theplatform.dfh.cp.modules.monitor.config;

public class ConfigKey<T>
{
    private String propertyKey;
    private T defaultValue;
    private Class<T> type;
    public ConfigKey(String propertyKey, T defaultValue, Class<T> type)
    {
        this.propertyKey = propertyKey;
        this.defaultValue = defaultValue;
        this.type = type;
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
