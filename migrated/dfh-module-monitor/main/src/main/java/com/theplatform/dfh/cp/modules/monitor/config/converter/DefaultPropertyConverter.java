package com.theplatform.dfh.cp.modules.monitor.config.converter;

import org.apache.commons.beanutils.ConvertUtils;

/**
 * Property converter that wraps the Apache commons ConvertUtils converter.
 * See https://commons.apache.org/proper/commons-beanutils/apidocs/org/apache/commons/beanutils/ConvertUtilsBean.html
 * @param <T> The type this converter creates
 */
public class DefaultPropertyConverter<T> implements ConfigPropertyConverter<T>
{
    private final Class<T> destinationType;

    public DefaultPropertyConverter(Class<T> destinationType)
    {
        this.destinationType = destinationType;
    }

    @Override
    public T convertPropertyValue(String propertyValue)
    {
        // TODO: According to https://commons.apache.org/proper/commons-beanutils/apidocs/org/apache/commons/beanutils/ConvertUtilsBean.html
        // invalid property values will map to defaults (zero/null) which makes defaulting in ConfigurationProperties impossible
        return destinationType.cast(ConvertUtils.convert(propertyValue, destinationType));
    }
}
