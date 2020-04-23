package com.comcast.pop.modules.sync.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CollectionUtilTest
{
    @DataProvider
    public Object[][] splitProvider()
    {
        return new Object[][]
            {
                { createList(0), 1},
                { createList(1), 1},
                { createList(11), 4},
                { createList(25), 5},
                { createList(26), 5},
            };
    }

    @Test(dataProvider = "splitProvider")
    public void testSplit(List<Integer> list, final int SUBSET_SIZE)
    {
        final int EXPECTED_SET_COUNT = (int)Math.ceil((double)list.size() / (double)SUBSET_SIZE);
        Collection<List<Integer>> splitCollection = CollectionUtil.split(list, SUBSET_SIZE);
        Assert.assertEquals(splitCollection.size(), EXPECTED_SET_COUNT);
    }

    @DataProvider
    public Object[][] splitInvalidInputProvider()
    {
        return new Object[][]
            {
                { createList(0), -1},
                { createList(0), 0},
                { null, 1},
            };
    }

    @Test(dataProvider = "splitInvalidInputProvider")
    public void testSplitInvalidInput(List<Integer> list, final int SUBSET_SIZE)
    {
        Assert.assertNull(CollectionUtil.split(list, SUBSET_SIZE));
    }

    public List<Integer> createList(int size)
    {
        return IntStream.range(0, size).boxed().collect(Collectors.toList());
    }
}
