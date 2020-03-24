package com.theplatform.dfh.persistence.aws.dynamodb;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class TableIndexesTest
{
    private final String SIMPLE_FIELD = "simple";
    private final String SORT_FIELD = "sort";
    private final String ANOTHER_FIELD = "another";
    private final TableIndex SIMPLE_FIELD_INDEX = new TableIndex(null, SIMPLE_FIELD, null);
    private final TableIndex COMPOSITE_INDEX = new TableIndex(null, SIMPLE_FIELD, SORT_FIELD);
    private final TableIndex ANOTHER_FIELD_INDEX = new TableIndex(null, ANOTHER_FIELD, null);
    private final TableIndex ANOTHER_FIELD_COMPOSITE_INDEX = new TableIndex(null, ANOTHER_FIELD, SORT_FIELD);

    @DataProvider
    public Object[][] getTableIndexProvider()
    {
        return new Object[][]
            {
                { genIndexes(SIMPLE_FIELD_INDEX), SIMPLE_FIELD, SIMPLE_FIELD_INDEX},
                { genIndexes(SIMPLE_FIELD_INDEX), ANOTHER_FIELD, null},
            };
    }

    @Test(dataProvider = "getTableIndexProvider")
    public void testGetTableIndex(TableIndexes tabledIndexes, String fieldName, TableIndex expectedIndex)
    {
        Assert.assertEquals(tabledIndexes.getTableIndex(fieldName), expectedIndex);
    }

    @DataProvider
    public Object[][] getBestTableIndexMatchProvider()
    {
        return new Object[][]
            {
                { genIndexes(SIMPLE_FIELD_INDEX), Arrays.asList(SIMPLE_FIELD), SIMPLE_FIELD_INDEX},
                { genIndexes(SIMPLE_FIELD_INDEX), Arrays.asList(SIMPLE_FIELD, SORT_FIELD), SIMPLE_FIELD_INDEX},
                { genIndexes(SIMPLE_FIELD_INDEX), Arrays.asList(SIMPLE_FIELD, SORT_FIELD, ANOTHER_FIELD), SIMPLE_FIELD_INDEX},
                { genIndexes(SIMPLE_FIELD_INDEX), Arrays.asList(ANOTHER_FIELD), null},
                { genIndexes(SIMPLE_FIELD_INDEX, COMPOSITE_INDEX), Arrays.asList(SIMPLE_FIELD, SORT_FIELD), COMPOSITE_INDEX},
                { genIndexes(SIMPLE_FIELD_INDEX, COMPOSITE_INDEX), Arrays.asList(SIMPLE_FIELD, SORT_FIELD, ANOTHER_FIELD), COMPOSITE_INDEX},
                { genIndexes(SIMPLE_FIELD_INDEX, COMPOSITE_INDEX), Arrays.asList(SIMPLE_FIELD), SIMPLE_FIELD_INDEX},
                { genIndexes(SIMPLE_FIELD_INDEX, COMPOSITE_INDEX), Arrays.asList(SIMPLE_FIELD, ANOTHER_FIELD), SIMPLE_FIELD_INDEX},
                { genIndexes(SIMPLE_FIELD_INDEX, COMPOSITE_INDEX, ANOTHER_FIELD_INDEX), Arrays.asList(SIMPLE_FIELD, ANOTHER_FIELD), SIMPLE_FIELD_INDEX},
                // confirm that the order of the fields results in the desired index if there are multiple possibilities
                { genIndexes(SIMPLE_FIELD_INDEX, COMPOSITE_INDEX, ANOTHER_FIELD_INDEX, ANOTHER_FIELD_COMPOSITE_INDEX),
                    Arrays.asList(ANOTHER_FIELD, SORT_FIELD, SIMPLE_FIELD),
                    ANOTHER_FIELD_COMPOSITE_INDEX},
            };
    }

    @Test(dataProvider = "getBestTableIndexMatchProvider")
    public void testGetBestTableIndexMatch(TableIndexes tabledIndexes, List<String> fields, TableIndex expectedIndex)
    {
        Assert.assertEquals(tabledIndexes.getBestTableIndexMatch(fields), expectedIndex);
    }

    private TableIndexes genIndexes(TableIndex... indexes)
    {
        TableIndexes tableIndexes = new TableIndexes();
        Arrays.asList(indexes).forEach(tableIndexes::withIndex);
        return tableIndexes;
    }
}
