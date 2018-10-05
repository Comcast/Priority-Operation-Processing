package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonReferenceReplacer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class JsonContextUpdaterTest
{
    JsonReferenceReplacer jsonReferenceReplacer = new JsonReferenceReplacer();

    @DataProvider
    public Object[][] getReferenceNameProvider()
    {
        return new Object[][]
            {
                { "@@item.out", "item"},
                { "@@item.out.out", "item.out"},
                { "@@item.out::/", "item"},
                { "@@item", "item"},
                { "@@item.out::/.out", "item"},
            };
    }

    @Test(dataProvider = "getReferenceNameProvider")
    public void testGetReferenceName(String reference, String expectedResult)
    {
        Assert.assertEquals(JsonContextUpdater.getReferenceName(jsonReferenceReplacer, reference), expectedResult);
    }

    @DataProvider
    public Object[][] getReferenceNameProviderErrorCases()
    {
        return new Object[][]
            {
                {"not.a.reference"},
                {null},
                {""},
            };
    }

    @Test(dataProvider = "getReferenceNameProviderErrorCases", expectedExceptions = RuntimeException.class)
    public void testGetReferenceNameException(String reference)
    {
        JsonContextUpdater.getReferenceName(jsonReferenceReplacer, reference);
    }
}
