package com.theplatform.dfh.persistence.aws.dynamodb;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TableIndexTest
{
    @DataProvider
    public Object[][] isPrimaryKeyProvider()
    {
        return new Object[][]
            {
                { genIndex("key", null), "key", true },
                { genIndex(null, "key"), "key", true },
                { genIndex(null, null), "key", false },
                { genIndex(null, null), null, false },
            };
    }

    @Test(dataProvider = "isPrimaryKeyProvider")
    public void testIsPrimaryKey(TableIndex tableIndex, String field, final boolean EXPECTED_RESULT)
    {
        Assert.assertEquals(tableIndex.isPrimaryKey(field), EXPECTED_RESULT);
    }

    private TableIndex genIndex(String partitionKey, String sortKey)
    {
        return new TableIndex(null, partitionKey, sortKey);
    }
}
