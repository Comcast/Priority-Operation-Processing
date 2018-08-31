package com.theplatform.dfh.cp.api.params;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ParamsMapTest
{
    private final AudioParamKey KEY = AudioParamKey.bitrate;

    private ParamsMap paramsMap;

    @BeforeMethod
    public void setup()
    {
        paramsMap = new ParamsMap();
    }

    @DataProvider
    public Object[][] getLongParamProvider()
    {
        return new Object[][]
            {
                {null, null},
                {45L, 45L},
                {45, 45L},
                {"45", 45L},
            };
    }

    @Test(dataProvider = "getLongParamProvider")
    public void testGetLongParam(Object valueToSet, Long expectedValue)
    {
        if(valueToSet != null) paramsMap.put(KEY, valueToSet);
        Assert.assertEquals(paramsMap.getLong(KEY), expectedValue);
    }

    @DataProvider
    public Object[][] getIntegerParamProvider()
    {
        return new Object[][]
            {
                {null, null},
                {45, 45},
                {"45", 45},
            };
    }

    @Test(dataProvider = "getIntegerParamProvider")
    public void testGetIntegerParam(Object valueToSet, Integer expectedValue)
    {
        if(valueToSet != null) paramsMap.put(KEY, valueToSet);
        Assert.assertEquals(paramsMap.getInt(KEY), expectedValue);
    }


    @DataProvider
    public Object[][] getDoubleParamProvider()
    {
        return new Object[][]
            {
                {null, null},
                {45.0, 45.0},
                {45.1, 45.1},
                {"45.1", 45.1},
                {"0.1", 0.1},
            };
    }

    @Test(dataProvider = "getDoubleParamProvider")
    public void testGetDoubleParam(Object valueToSet, Double expectedValue)
    {
        if(valueToSet != null) paramsMap.put(KEY, valueToSet);
        Assert.assertEquals(paramsMap.getDouble(KEY), expectedValue);
    }


    @DataProvider
    public Object[][] getBooleanParamProvider()
    {
        return new Object[][]
            {
                {null, null},
                {true, true},
                {Boolean.TRUE, true},
                {"true", true},
                {"false", false},
            };
    }

    @Test(dataProvider = "getBooleanParamProvider")
    public void testGetBooleanParam(Object valueToSet, Boolean expectedValue)
    {
        if(valueToSet != null) paramsMap.put(KEY, valueToSet);
        Assert.assertEquals(paramsMap.getBoolean(KEY), expectedValue);
    }

}
