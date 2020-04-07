package com.comcast.pop.agenda.reclaim.aws.config;

import com.comcast.pop.agenda.reclaim.config.ReclaimerConfig;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AWSReclaimerConfigTest
{
    @DataProvider
    public Object[][] validateProvider()
    {
        return new Object[][]
            {
                { new AWSReclaimerConfig().setTableName(null), "tableName"},
                { new AWSReclaimerConfig().setIdFieldName(null), "idFieldName"},
                { new AWSReclaimerConfig().setTimeFieldName(null), "timeFieldName"},
                { new AWSReclaimerConfig().setReclaimAgeMinutes(-1), "reclaimAgeMinutes"},
                { new AWSReclaimerConfig().setScanDelayMillis(-1), "scanDelayMillis"},
                { new AWSReclaimerConfig().setTargetBatchSize(-1), "targetBatchSize"},
                { new AWSReclaimerConfig().setObjectScanLimit(-1), "objectScanLimit"}
            };
    }

    @Test(dataProvider = "validateProvider")
    public void testValidate(AWSReclaimerConfig config, final String EXPECTED_FRAGMENT)
    {
        String validationResult = config.validate();
        Assert.assertTrue(StringUtils.containsIgnoreCase(validationResult, EXPECTED_FRAGMENT));
    }

    @Test
    public void testValidate()
    {
        ReclaimerConfig config = new AWSReclaimerConfig()
            .setTableName("Table")
            .setIdFieldName("id")
            .setTimeFieldName("timeField");
        Assert.assertNull(config.validate());
    }
}
