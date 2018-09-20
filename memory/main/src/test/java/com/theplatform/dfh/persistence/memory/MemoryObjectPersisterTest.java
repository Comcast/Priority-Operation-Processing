package com.theplatform.dfh.persistence.memory;

import com.theplatform.dfh.persistence.memory.MemoryObjectPersister;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MemoryObjectPersisterTest
{
    private MemoryObjectPersister<String> memoryObjectPersister;

    @BeforeMethod
    public void setup()
    {
        memoryObjectPersister = new MemoryObjectPersister<>();
    }

    @Test
    public void testBasicActions()
    {
        final String key = "theKey";
        final String value = "theValue";
        Assert.assertNull(memoryObjectPersister.retrieve(key));
        memoryObjectPersister.persist(key, value);
        Assert.assertEquals(memoryObjectPersister.retrieve(key), value);
        memoryObjectPersister.delete(key);
        Assert.assertNull(memoryObjectPersister.retrieve(key));
    }
}
