package com.comcast.pop.agenda.reclaim.config;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ReclaimerConfigTest
{
    @DataProvider
    public Object[][] validateProvider()
    {
        return new Object[][]
            {
                { new ReclaimerConfig().setMaximumExecutionSeconds(-1), "MaximumExecutionSeconds"},
            };
    }

    @Test(dataProvider = "validateProvider")
    public void testValidate(ReclaimerConfig config, final String EXPECTED_FRAGMENT)
    {
        String validationResult = config.validate();
        Assert.assertTrue(StringUtils.containsIgnoreCase(validationResult, EXPECTED_FRAGMENT));
    }
}
