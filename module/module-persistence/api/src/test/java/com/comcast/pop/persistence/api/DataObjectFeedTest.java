package com.comcast.pop.persistence.api;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class DataObjectFeedTest
{
    private DataObjectFeed<String> dataObjectFeed;

    @BeforeMethod
    public void setup()
    {
        dataObjectFeed = new DataObjectFeed<>();
    }

    @Test
    public void testAddAll()
    {
        List<String> inputs = Arrays.asList("1", "2", "3");
        List<String> moreInputs = Arrays.asList("4", "5", "6");
        dataObjectFeed.addAll(inputs);
        Assert.assertTrue(dataObjectFeed.getAll().containsAll(inputs), "Missing inputs in the DataObjectFeed");

        // verify no overwrites etc.
        dataObjectFeed.addAll(moreInputs);
        Assert.assertTrue(dataObjectFeed.getAll().containsAll(inputs), "Missing inputs in the DataObjectFeed");
        Assert.assertTrue(dataObjectFeed.getAll().containsAll(moreInputs), "Missing extra inputs in the DataObjectFeed");
    }
}
