package com.theplatform.dfh.cp.api.params;


import java.util.HashMap;

public class ParamsMap extends HashMap<String, Object>
{
    public String getString(final ParamKey fieldEnum)
    {
        return getString(fieldEnum.getKey(), null);
    }
    public String getString(final ParamKey fieldEnum, final String defaultValue)
    {
        return getString(fieldEnum.getKey(), defaultValue);
    }
    public String getString(final String name)
    {
        return getString(name, null);
    }
    public String getString(final String name, final String defaultValue)
    {
        Object value = getOrDefault(name, defaultValue);
        if(value == null) return defaultValue;
        return value.toString();
    }

    public Long getLong(ParamKey fieldEnum)
    {
        return getLong(fieldEnum.getKey(), null);
    }
    public Long getLong(ParamKey fieldEnum, Long defaultValue)
    {
        return getLong(fieldEnum.getKey(), defaultValue);
    }
    public Long getLong(String key)
    {
        return getLong(key, null);
    }
    public Long getLong(String key, Long defaultValue)
    {
        Object param = getOrDefault(key, defaultValue);
        if(param == null)
            return defaultValue;
        if(param instanceof Long)
            return (Long) param;

        return Long.parseLong(param.toString());
    }

    public Integer getInt(ParamKey fieldEnum)
    {
        return getInt(fieldEnum.getKey(), null);
    }
    public Integer getInt(ParamKey fieldEnum, Integer defaultValue)
    {
        return getInt(fieldEnum.getKey(), defaultValue);
    }
    public Integer getInt(String key)
    {
        return getInt(key, null);
    }
    public Integer getInt(String key, Integer defaultValue)
    {
        Object param = getOrDefault(key, defaultValue);
        if(param == null)
            return defaultValue;
        if(param instanceof Integer)
            return (Integer) param;

        return Integer.parseInt(param.toString());
    }

    public Boolean getBoolean(ParamKey fieldEnum)
    {
        return getBoolean(fieldEnum.getKey(), null);
    }
    public Boolean getBoolean(ParamKey fieldEnum, Boolean defaultValue)
    {
        return getBoolean(fieldEnum.getKey(), defaultValue);
    }
    public Boolean getBoolean(String key)
    {
        return getBoolean(key, null);
    }
    public Boolean getBoolean(String key, Boolean defaultValue)
    {
        Object param = get(key);
        if(param == null)
            return defaultValue;
        if(param instanceof Boolean)
            return (Boolean) param;

        return Boolean.parseBoolean(param.toString());
    }

    public Double getDouble(ParamKey fieldEnum)
    {
        return getDouble(fieldEnum.getKey(), null);
    }
    public Double getDouble(ParamKey fieldEnum, Double defaultValue)
    {
        return getDouble(fieldEnum.getKey(), defaultValue);
    }
    public Double getDouble(String key)
    {
        return getDouble(key, null);
    }
    public Double getDouble(String key, Double defaultValue)
    {
        Object param = get(key);
        if(param == null)
            return defaultValue;
        if(param instanceof Double)
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

    public void putIfNonNull(Enum key, Object value)
    {
        putIfNonNull(key.name(), value);
    }
    public void putIfNonNull(String key, Object value)
    {
        if(value != null) put(key, value);
    }

    public void copyKeyIfPresent(ParamsMap destinationMap, ParamKey fieldEnum)
    {
        copyKeyIfPresent(destinationMap, fieldEnum.getKey());
    }
    public void copyKeyIfPresent(ParamsMap destinationMap, String key)
    {
        if(containsKey(key)) destinationMap.put(key, get(key));
    }
}
