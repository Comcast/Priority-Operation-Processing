package com.theplatform.dfh.cp.modules.monitor.config;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts a properties object to a key / value storage. The values are typed and validated.
 * Properties are converted using ConvertUtilsBean. The documentation indicates the supported types and defaults.
 * https://commons.apache.org/proper/commons-beanutils/apidocs/org/apache/commons/beanutils/ConvertUtilsBean.html
 */
public class ConfigurationProperties
{
    private final Map<ConfigKey<?>, Object> configMap = new HashMap<>();

    public ConfigurationProperties()
    {
    }

    public <T> void put( ConfigKey<T> key, T value ) {
        if(key == null)
            return;
        configMap.put( key, value );
    }

    public <T> T get( ConfigKey<T> key ) {
        if(key == null)
            return null;
        return key.getType().cast( configMap.get( key ) );
    }

    public Set<ConfigKey<?>> getKeys()
    {
        return configMap.keySet();
    }

    public static ConfigurationProperties from(Properties properties, ConfigKeys... configKeys)
    {
        ConfigurationProperties config = new ConfigurationProperties();
        if(configKeys != null)
        {
            for(ConfigKeys keys : configKeys)
                config.load(keys.getKeys(), properties);
        }
        else
        {
            config.load(null, properties);
        }
        return config;
    }

    private void load(Set<ConfigKey> keys, Properties properties)
    {
        Map<String, ConfigKey> configKeyStrings = keys == null ? new HashMap<>() : keys.stream()
                .collect(Collectors.toMap(configKey -> configKey.getPropertyKey(), configKey -> configKey));

        Enumeration propertyStringKeys = properties.propertyNames();
        if(properties == null) return;

        for(; propertyStringKeys.hasMoreElements(); )
        {
            Object propertyKey = propertyStringKeys.nextElement();
            String propertyValue = properties.getProperty(propertyKey.toString());

            ConfigKey configKeyForProperty = configKeyStrings.get(propertyKey);
            //if configKeyForProperty is null, then it's something we don't have defined in our key class.
            if(configKeyForProperty == null)
            {
                 put(new ConfigKey(propertyKey.toString(), null, String.class), propertyValue);
            }
            else if (StringUtils.isBlank(propertyValue) && configKeyForProperty.getDefaultValue() != null)
            {
                put(configKeyForProperty, configKeyForProperty.getDefaultValue());
            }
            else
            {
                if (configKeyForProperty.getType() == String[].class)
                {
                    put(configKeyForProperty, propertyValue.split(","));
                }
                else
                {
                    Object value = ConvertUtils.convert(propertyValue, configKeyForProperty.getType());
                    if(value == null)
                    {
                        value = configKeyForProperty.getDefaultValue();
                    }
                    //bad integers will become zero, don't use default???
                    put(configKeyForProperty, configKeyForProperty.getType() != null ? value : propertyValue);
                }
            }
        }
    }

}
