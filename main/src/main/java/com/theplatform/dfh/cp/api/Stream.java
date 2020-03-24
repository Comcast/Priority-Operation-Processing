package com.theplatform.dfh.cp.api;

import java.util.Map;

public interface Stream
{
    public String getReference();

    public void setReference(String sourceStreamReference);

    public String getType();

    public void setType(String type);

    public Map<String, Object> getParams();

    public String getString(final String name);
}
