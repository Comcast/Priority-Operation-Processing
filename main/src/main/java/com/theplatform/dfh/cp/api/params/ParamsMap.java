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
        return getLong(fieldEnum.getKey());
    }
    public Long getLong(String key)
    {
        Object param = get(key);
        if(param == null || param instanceof Long)
            return (Long) param;

        return Long.parseLong(param.toString());
    }
    public Integer getInt(ParamKey fieldEnum)
    {
        return getInt(fieldEnum.getKey());
    }
    public Integer getInt(String key)
    {
        Object param = get(key);
        if(param == null || param instanceof Integer)
            return (Integer) param;

        return Integer.parseInt(param.toString());
    }
    public Boolean getBoolean(ParamKey fieldEnum)
    {
        return getBoolean(fieldEnum.getKey());
    }
    public Boolean getBoolean(String key)
    {
        Object param = get(key);
        if(param == null || param instanceof Boolean)
            return (Boolean) param;

        return Boolean.parseBoolean(param.toString());
    }
    public Double getDouble(ParamKey fieldEnum)
    {
        return getDouble(fieldEnum.getKey());
    }
    public Double getDouble(String key)
    {
        Object param = get(key);
        if(param == null || param instanceof Double)
            return (Double) param;

        return Double.parseDouble(param.toString());
    }
    public Object get(final ParamKey key)
    {
        return get(key.getKey());
    }
    public Object put(Enum key, Object value)
    {
        return put(key.name(), value);
    }

    public Boolean containsKey(ParamKey fieldEnum) { return containsKey(fieldEnum.getKey()); }
}
