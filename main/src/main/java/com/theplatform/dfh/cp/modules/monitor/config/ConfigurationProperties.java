package com.theplatform.dfh.cp.modules.monitor.config;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts a properties object to a key / value storage. The values are typed and validated.
 */
public class ConfigurationProperties
{
    private final Map<ConfigKey, Object> configMap = new HashMap<>();

    public ConfigurationProperties()
    {
    }

    public <T> void put(ConfigKey key, T value) {
        if(key == null)
            return;
        configMap.put( key, value );
    }

    public <T> T get(ConfigKey<T> key ) {
        if(key == null)
            return null;
        return key.getType().cast( configMap.get( key ) );
    }

    public Set<ConfigKey> getKeys()
    {
        return configMap.keySet();
    }

    public static ConfigurationProperties from(Properties properties, ConfigKeys... configKeys)
    {
        ConfigurationProperties config = new ConfigurationProperties();
        if(properties == null) return config;

        if(configKeys != null)
        {
            //If there are supplied keys we load properties from each
            Set<ConfigKey> allKeys = new HashSet<>();
            for(ConfigKeys<?> keys : configKeys)
                allKeys.addAll(keys.getKeys());

            config.load(allKeys, properties);
        }
        else
        {
            //There are no supplied keys so just load properties with generic config key
            config.load(null, properties);
        }
        return config;
    }

    private void load(Set<ConfigKey> keys, Properties properties)
    {
        Map<String, ConfigKey> configKeyStrings = keys == null ? new HashMap<>() : keys.stream()
            .collect(Collectors.toMap(configKey -> configKey.getPropertyKey(), configKey -> configKey));

        Enumeration propertyStringKeys = properties.propertyNames();
        if(propertyStringKeys == null) return;

        //Loop through all Properites.properties. If a key matches, use it for our map, if not make a generic one.
        while(propertyStringKeys.hasMoreElements())
        {
            Object property = propertyStringKeys.nextElement();
            String propertyName = property.toString();
            String propertyValue = properties.getProperty(propertyName);

            ConfigKey configKeyForProperty = configKeyStrings.remove(propertyName);
            //if configKeyForProperty is null, then it's something we don't have defined in our key class.
            if(configKeyForProperty == null)
            {
                put(new ConfigKey<>(propertyName, null, String.class), propertyValue);
            }
            else if (StringUtils.isBlank(propertyValue) && configKeyForProperty.getDefaultValue() != null)
            {
                put(configKeyForProperty, configKeyForProperty.getDefaultValue());
            }
            else
            {
                Object value = configKeyForProperty.getConfigPropertyConverter().convertPropertyValue(propertyValue);
                if(value == null)
                    value = configKeyForProperty.getDefaultValue();

                put(configKeyForProperty, value);
            }
        }
        //If any of our supplied config keys are left over after loading properties
        //we set any that have defaults configured. So if "myprop.x" is not found in the properties
        //file we still set a lookup with it's default value found int he config class.
        for(Map.Entry<String, ConfigKey> configKey : configKeyStrings.entrySet())
        {
            if(configKey.getValue().getDefaultValue() != null)
                put(configKey.getValue(), configKey.getValue().getDefaultValue());
        }
    }

}
