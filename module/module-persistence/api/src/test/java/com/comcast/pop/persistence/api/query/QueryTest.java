package com.comcast.pop.persistence.api.query;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class QueryTest
{
    @DataProvider
    public Object[][] valueEncodingProvider()
    {
        return new Object[][]
            {
                {"1234", "1234"},
                {"http://identity/stuff/1234", "http%3A%2F%2Fidentity%2Fstuff%2F1234"}
            };
    }

    @Test(dataProvider = "valueEncodingProvider")
    public void testURLEncodeOnValue(final String inputValue, final String expectedQueryParam) throws Exception
    {
        Query<String> query = new Query<>("", inputValue);
        Assert.assertEquals(query.toQueryParam(), "by=" + expectedQueryParam);
    }
}
