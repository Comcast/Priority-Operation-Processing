package com.theplatform.dfh.cp.api;

import java.util.HashMap;

public class ParamsMap extends HashMap<String, Object>
{
    public String getString(final Enum fieldEnum)
    {
        return getString(fieldEnum.name());
    }

    public String getString(final String name)
    {
        return (String)get(name);
    }
    public Long getLong(Enum fieldEnum)
    {
        Object param = getParam(fieldEnum.name());
        if(param instanceof Long)
            return (Long) param;

        return Long.parseLong(param.toString());
    }
    public Integer getInt(Enum fieldEnum)
    {
        return (Integer)getParam(fieldEnum.name());
    }
    private Object getParam(final String name)
    {
        return get(name);
    }

    public Object put(Enum key, Object value)
    {
        return super.put(key.name(), value);
    }
}
