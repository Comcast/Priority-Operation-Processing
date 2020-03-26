package com.theplatform.dfh.cp.modules.monitor.config.converter;

/**
 * Converter from a String to a String[] based on the delimiter specified.
 */
public class StringArrayPropertyConverter implements ConfigPropertyConverter<String[]>
{
    private final String delimiterRegex;

    public StringArrayPropertyConverter(String delimiterRegex)
    {
        this.delimiterRegex = delimiterRegex;
    }

    @Override
    public String[] convertPropertyValue(String propertyValue)
    {
        if(propertyValue != null)
            return propertyValue.split(delimiterRegex);
        return null;
    }
}
