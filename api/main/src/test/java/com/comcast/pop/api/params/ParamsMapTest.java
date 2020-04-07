package com.comcast.pop.api.params;

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
    public Object[][] getStringParamProvider()
    {
        return new Object[][]
            {
                {null, null},
                {"theString", "theString"},
            };
    }

    @Test(dataProvider = "getStringParamProvider")
    public void testGetStringParam(Object valueToSet, String expectedValue)
    {
        if(valueToSet != null) paramsMap.put(KEY, valueToSet);
        Assert.assertEquals(paramsMap.getString(KEY), expectedValue);
        Assert.assertEquals(paramsMap.getString(KEY.name()), expectedValue);
    }

    @DataProvider
    public Object[][] getStringParamDefaultProvider()
    {
        return new Object[][]
            {
                {null, null, null},
                {null, "theString", "theString"},
                {"theString1", "theString2", "theString1"},
            };
    }

    @Test(dataProvider = "getStringParamDefaultProvider")
    public void testGetStringParamDefault(Object valueToSet, String defaultValue, String expectedValue)
    {
        if(valueToSet != null) paramsMap.put(KEY, valueToSet);
        Assert.assertEquals(paramsMap.getString(KEY, defaultValue), expectedValue);
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
        Assert.assertEquals(paramsMap.getLong(KEY.name()), expectedValue);
    }

    @DataProvider
    public Object[][] getLongParamDefaultProvider()
    {
        return new Object[][]
            {
                {null, null, null},
                {null, 45L, 45L},
                {"45", 55L, 45L},
            };
    }

    @Test(dataProvider = "getLongParamDefaultProvider")
    public void testGetLongDefaultParam(Object valueToSet, Long defaultValue, Long expectedValue)
    {
        if(valueToSet != null) paramsMap.put(KEY, valueToSet);
        Assert.assertEquals(paramsMap.getLong(KEY, defaultValue), expectedValue);
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
        Assert.assertEquals(paramsMap.getInt(KEY.name()), expectedValue);
    }

    @DataProvider
    public Object[][] getIntegerParamDefaultProvider()
    {
        return new Object[][]
            {
                {null, null, null},
                {null, 45, 45},
                {"45", 55, 45},
            };
    }

    @Test(dataProvider = "getIntegerParamDefaultProvider")
    public void testGetIntegerDefaultParam(Object valueToSet, Integer defaultValue, Integer expectedValue)
    {
        if(valueToSet != null) paramsMap.put(KEY, valueToSet);
        Assert.assertEquals(paramsMap.getInt(KEY, defaultValue), expectedValue);
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
        Assert.assertEquals(paramsMap.getDouble(KEY.name()), expectedValue);
    }

    @DataProvider
    public Object[][] getDoubleParamDefaultProvider()
    {
        return new Object[][]
            {
                {null, null, null},
                {null, 45.1, 45.1},
                {"45.5", 55.0, 45.5},
            };
    }

    @Test(dataProvider = "getDoubleParamDefaultProvider")
    public void testGetDoubleDefaultParam(Object valueToSet, Double defaultValue, Double expectedValue)
    {
        if(valueToSet != null) paramsMap.put(KEY, valueToSet);
        Assert.assertEquals(paramsMap.getDouble(KEY, defaultValue), expectedValue);
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
        Assert.assertEquals(paramsMap.getBoolean(KEY.name()), expectedValue);
    }

    @DataProvider
    public Object[][] getBooleanParamDefaultProvider()
    {
        return new Object[][]
            {
                {null, null, null},
                {true, true, true},
                {Boolean.TRUE, true, true},
                {"true", true, true},
                {"false", true, false},
            };
    }

    @Test(dataProvider = "getBooleanParamDefaultProvider")
    public void testGetBooleanDefaultParam(Object valueToSet, Boolean defaultValue, Boolean expectedValue)
    {
        if(valueToSet != null) paramsMap.put(KEY, valueToSet);
        Assert.assertEquals(paramsMap.getBoolean(KEY, defaultValue), expectedValue);
    }

}
