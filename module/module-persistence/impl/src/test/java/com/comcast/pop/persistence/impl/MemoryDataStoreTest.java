package com.comcast.pop.persistence.impl;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MemoryDataStoreTest
{
    private MemoryObjectPersister<PersistenceTestObject> memoryDataStore;

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
        PersistenceTestObject testObject = new PersistenceTestObject();
        testObject.setId(key);
        testObject.setVal(value);

        Assert.assertNull(memoryDataStore.retrieve(key));
        memoryDataStore.persist(testObject);
        Assert.assertEquals(memoryDataStore.retrieve(key).getVal(), value);
        memoryDataStore.delete(key);
        Assert.assertNull(memoryDataStore.retrieve(key));
    }
}
