package com.theplatform.dfh.cp.modules.monitor.bananas.config;

import com.theplatform.dfh.cp.modules.monitor.config.ConfigKey;
import com.theplatform.dfh.cp.modules.monitor.config.converter.ConfigPropertyConverter;

public class MessageAttributeKey<TYPE> extends ConfigKey<TYPE>
{
    private String attributeKey;

    public MessageAttributeKey(String propertyKey, TYPE defaultValue, String attributeKey, Class<TYPE> type)
    {
        super(propertyKey, defaultValue, type);
        this.attributeKey = attributeKey;
    }

    public MessageAttributeKey(String propertyKey, TYPE defaultValue, String attributeKey, Class<TYPE> type, ConfigPropertyConverter<TYPE> configPropertyConverter)
    {
        super(propertyKey, defaultValue, type, configPropertyConverter);
        this.attributeKey = attributeKey;
    }

    public String getAttributeKey()
    {
        return attributeKey;
    }
}
