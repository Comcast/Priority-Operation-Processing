package com.theplatform.dfh.cp.api.params;


import java.util.HashMap;

public class ParamsMap extends HashMap<String, Object>
{

    public String getString(final ParamKey fieldEnum)
    {
        return getString(fieldEnum.getKey());
    }

    public String getString(final String name)
    {
        Object value = get(name);
        if(value == null) return null;
        return value.toString();
    }
    public Long getLong(ParamKey fieldEnum)
    {
        Object param = get(fieldEnum.getKey());
        if(param instanceof Long)
            return (Long) param;

        return Long.parseLong(param.toString());
    }
    public Integer getInt(ParamKey fieldEnum)
    {
        Object param = get(fieldEnum.getKey());
        if(param instanceof Integer)
            return (Integer) param;

        return Integer.parseInt(param.toString());
    }
    public Boolean getBoolean(ParamKey fieldEnum)
    {
        Object param = get(fieldEnum.getKey());
        if(param instanceof Boolean)
            return (Boolean) param;

        return Boolean.parseBoolean(param.toString());
    }
    public Object get(final ParamKey key)
    {
        return get(key.getKey());
    }
    public Object put(Enum key, Object value)
    {
        return put(key.name(), value);
    }
}
