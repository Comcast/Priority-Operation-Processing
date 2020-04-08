package com.comcast.pop.modules.monitor.config.converter;

public interface ConfigPropertyConverter<T>
{
    /**
     * Converts the input string to the type associated with this converter
     * @param propertyValue The string to convert
     * @return The result of the conversion
     */
    T convertPropertyValue(String propertyValue);
}
