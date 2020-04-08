package com.comcast.pop.cp.endpoint.resourcepool.insight.mapper;

import com.comcast.pop.api.facility.InsightMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public enum InsightMapperRegistry
{
    operationName(OperationNameMapper.class), operationType(OperationTypeMapper.class), paramMap(ParamMapMapper.class);

    private static final Logger logger = LoggerFactory.getLogger(InsightMapperRegistry.class);

    private final Class mapperClass;

    private InsightMapperRegistry(Class mapperClass)
    {
        this.mapperClass = mapperClass;
    }

    public Class getMapperClass()
    {
        return mapperClass;
    }

    public static InsightMapperRegistry fromName(String mapperName)
    {
        return Arrays.stream(InsightMapperRegistry.values())
                .filter(e -> e.name().equalsIgnoreCase(mapperName)).findAny().orElse(null);
    }

    public static InsightMapper getMapper(String name, Set<String> values)
    {
        if(name == null) return null;

        InsightMapperRegistry mapperEnum = InsightMapperRegistry.fromName(name);
        if(mapperEnum != null)
        {
            try
            {
                AbstractInsightMapper insightMapper = (AbstractInsightMapper)mapperEnum.getMapperClass().newInstance();
                if(values != null)
                    values.stream().forEach(v -> insightMapper.withMatchValue(v));
                return insightMapper;
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                logger.error("This is a bug.", e);
            }
        }
        return null;
    }
}
