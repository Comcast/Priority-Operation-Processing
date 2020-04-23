package com.comcast.pop.api.params;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseParamKeyTest
{
    private ObjectMapper objectMapper = new ObjectMapper();

    public void testParamKeysMatchParamsObject(ParamKey[] paramKeys, Object paramsObject)
    {
        Map<String, Object> map = objectMapper.convertValue(paramsObject, new TypeReference<Map<String, Object>>() {});
        Set<String> paramKeySet = new HashSet<>(Arrays.stream(paramKeys).map(ParamKey::getKey).collect(Collectors.toList()));
        paramKeySet
            .forEach(key -> Assert.assertTrue(map.containsKey(key),
            String.format("[%1$s] Missing field: %2$s", paramsObject.getClass().getSimpleName(), key)
            ));
        map.keySet()
            .forEach(key -> Assert.assertTrue(paramKeySet.contains(key),
            String.format("[%1$s] Unnecessary field: %2$s", paramsObject.getClass().getSimpleName(), key)
            ));
    }
}
