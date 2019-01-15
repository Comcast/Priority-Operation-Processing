package com.theplatform.dfh.cp.endpoint.facility.insight.mapper;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.InsightMapper;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;

public class InsightMapperRegistryTest
{
    private static final Insight insight = Mockito.mock(Insight.class);
    @Test
    public void testNull()
    {
       Assert.assertNull(InsightMapperRegistry.getMapper(null,null));
    }
    @Test
    public void testOperationTypeMapper()
    {
        InsightMapper mapper = assertGetMappers(InsightMapperRegistry.operationType.name(), null);
       Assert.assertTrue(mapper instanceof OperationTypeMapper);
    }

    @Test
    public void testOperationNameMapper()
    {
        InsightMapper mapper = assertGetMappers(InsightMapperRegistry.operationName.name(), null);
        Assert.assertTrue(mapper instanceof OperationNameMapper);
        mapper = assertGetMappers("OPERATIONNAME", null);
        Assert.assertTrue(mapper instanceof OperationNameMapper);
    }

    private InsightMapper assertGetMappers(String mapperName, Set<String> values)
    {
        InsightMapper insightMapper = InsightMapperRegistry.getMapper(mapperName, values);
        Assert.assertNotNull(insightMapper);
        return insightMapper;
    }
}
