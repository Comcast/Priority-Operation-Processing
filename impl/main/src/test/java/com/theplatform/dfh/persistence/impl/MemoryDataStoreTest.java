package com.theplatform.dfh.persistence.impl;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MemoryDataStoreTest
{
    private MemoryObjectPersister<String> memoryDataStore;

    @BeforeMethod
    public void setup()
    {
        memoryDataStore = new MemoryObjectPersister<>();
    }

    @Test
    public void testBasicActions()
    {
        final String key = "theKey";
        final String value = "theValue";
        Assert.assertNull(memoryDataStore.retrieve(key));
        memoryDataStore.persist(key, value);
        Assert.assertEquals(memoryDataStore.retrieve(key), value);
        memoryDataStore.delete(key);
        Assert.assertNull(memoryDataStore.retrieve(key));
    }
}
