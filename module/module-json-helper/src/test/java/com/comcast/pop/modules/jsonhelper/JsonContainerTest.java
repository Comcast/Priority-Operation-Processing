package com.comcast.pop.modules.jsonhelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

public class JsonContainerTest
{
    @DataProvider
    public Object[][] constructorProvider()
    {
        return new Object[][]
            {
                {"", true},
                {"{}", false},
            };
    }

    @Test(dataProvider = "constructorProvider")
    public void testConstructor(String input, boolean expectException)
    {
        try
        {
            new JsonContainer(input);
            Assert.assertFalse(expectException);
        }
        catch(IOException e)
        {
            Assert.assertTrue(expectException);
        }
    }

    @Test
    public void testGetObject() throws Exception
    {
        ObjectMapper objectMapper = new ObjectMapper();
        final String ROOT_ID = "rootId";
        final String SUB_ID = "subId";

        final SampleObject sample = new SampleObject();
        sample.setId(ROOT_ID);
        final SampleObject subSample = new SampleObject();
        subSample.setId(SUB_ID);
        sample.setSubObject(subSample);
        JsonContainer jsonContainer = new JsonContainer(objectMapper.writeValueAsString(sample));
        SampleObject result  = jsonContainer.getObject(SampleObject.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), sample.getId());
        Assert.assertEquals(result.getSubObject().getId(), sample.getSubObject().getId());
    }
}
