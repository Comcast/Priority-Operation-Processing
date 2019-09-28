package com.theplatform.dfh.cp.endpoint.facility.insight.mapper;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper.OperationNameMapper;
import com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper.OperationTypeMapper;
import com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper.ParamMapMapper;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

public class InsightMapperTest
{
    private static final String OPERATION = "encoding";
    private static final Agenda agenda = Mockito.mock(Agenda.class);

    @Test
    public void testOperationTypeMapper()
    {
        Operation operation = new Operation();
        operation.setType(OPERATION);
        Mockito.when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));
        
        Assert.assertFalse(new OperationNameMapper().matches(agenda));
        Assert.assertFalse(new OperationTypeMapper().matches(agenda));
        Assert.assertTrue(new OperationTypeMapper().withMatchValue(OPERATION).matches(agenda));

    }

    @Test
    public void testOperationNameMapper()
    {
        Operation operation = new Operation();
        operation.setName(OPERATION);
        Mockito.when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));

        Assert.assertFalse(new OperationNameMapper().matches(agenda));
        Assert.assertFalse(new OperationTypeMapper().matches(agenda));
        Assert.assertTrue(new OperationNameMapper().withMatchValue(OPERATION).matches(agenda));

    }

    @Test
    public void testParamsMapMapper()
    {
        ParamsMap paramsMap = new ParamsMap();
        paramsMap.put("myparam","myvalue");
        Operation operation = new Operation();
        operation.setParams(paramsMap);
        Mockito.when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));

        Assert.assertFalse(new ParamMapMapper().matches(agenda));
        Assert.assertTrue(new ParamMapMapper().withMatchValue("myparam=mYvalue").matches(agenda));
        Assert.assertFalse(new ParamMapMapper().withMatchValue("myotherparam=myvalue").matches(agenda));

        Mockito.when(agenda.getParams()).thenReturn(paramsMap);
        Assert.assertTrue(new ParamMapMapper().withMatchValue("myparam=mYvalue").matches(agenda));
        Assert.assertFalse(new ParamMapMapper().withMatchValue("myotherparam=myvalue").matches(agenda));
    }
}
