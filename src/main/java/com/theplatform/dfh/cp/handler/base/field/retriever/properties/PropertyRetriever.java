package com.theplatform.dfh.cp.handler.base.field.retriever.properties;

import com.theplatform.dfh.cp.handler.base.field.retriever.api.FieldRetriever;

public class PropertyRetriever extends FieldRetriever
{
    private PropertyProvider propertyProvider;

    public PropertyRetriever(String filePath)
    {
        propertyProvider = new PropertyProvider(filePath);
    }

    @Override
    public String getField(String field)
    {
        return propertyProvider.getProperty(field, null);
    }

    @Override
    public String getField(String field, String defaultValue)
    {
        return propertyProvider.getProperty(field, defaultValue);
    }

    @Override
    public boolean isFieldSet(String field)
    {
        return getField(field, null) != null;
    }

    public void setPropertyProvider(PropertyProvider propertyProvider)
    {
        this.propertyProvider = propertyProvider;
    }

    public PropertyProvider getPropertyProvider()
    {
        return propertyProvider;
    }
}
