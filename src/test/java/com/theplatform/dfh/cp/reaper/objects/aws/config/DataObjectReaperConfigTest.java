package com.theplatform.dfh.cp.reaper.objects.aws.config;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataObjectReaperConfigTest
{
    @DataProvider
    public Object[][] validateProvider()
    {
        return new Object[][]
            {
                { new DataObjectReaperConfig().setTargetBatchSize(0), "batchSize" },
                { new DataObjectReaperConfig().setIdFieldName(""), "idFieldName" },
                { new DataObjectReaperConfig().setTableName(""), "tableName" },
                { new DataObjectReaperConfig().setTimeFieldName(""), "timeFieldName" },
                { new DataObjectReaperConfig().setDeleteCallDelayMillis(-1), "deleteCallDelayMillis" },
                { new DataObjectReaperConfig().setObjectScanLimit(-1), "objectScanLimit" },
                { new DataObjectReaperConfig().setReapAgeMinutes(-1), "reapAgeMinutes" },
                { new DataObjectReaperConfig().setScanDelayMillis(-1), "scanDelayMillis" },
                { new DataObjectReaperConfig().setMaximumExecutionSeconds(-1), "maximumExecutionSeconds" },
            };
    }

    @Test(dataProvider = "validateProvider")
    public void testValidate(DataObjectReaperConfig config, String expectedFieldName)
    {
        String result = config.validate();
        Assert.assertNotNull(result);
        Assert.assertTrue(StringUtils.containsIgnoreCase(result, expectedFieldName));
    }

    @Test
    public void testSuccessfulValidate()
    {
        DataObjectReaperConfig config = new DataObjectReaperConfig()
            .setTableName("A")
            .setIdFieldName("id")
            .setTimeFieldName("updatedTime");
        Assert.assertNull(config.validate());
    }
}
