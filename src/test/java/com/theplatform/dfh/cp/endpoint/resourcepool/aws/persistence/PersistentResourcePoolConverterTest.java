package com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence;

import com.theplatform.dfh.cp.api.facility.ResourcePool;
import org.apache.commons.beanutils.BeanMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.Map;

/**
 */
public class PersistentResourcePoolConverterTest
{
    PersistentResourcePoolConverter converter = new PersistentResourcePoolConverter();
    private static final String CLASS_FIELD = "class";

    @Test
    public void testToPersistenceConvert()
    {
        ResourcePool resourcePool = DataGenerator.generate();
        PersistentResourcePool persistentResourcePool = converter.getPersistentObject(resourcePool);
        assertEqual(resourcePool, persistentResourcePool);
    }

    private void assertEqual(Object orig, Object dest)
    {
        BeanMap beanMapOrig = new BeanMap(orig);
        BeanMap beanMapDest = new BeanMap(dest);
        Iterator<Map.Entry<Object, Object>> propertyIterator = beanMapOrig.entryIterator();
        while (propertyIterator.hasNext())
        {
            Map.Entry<Object, Object> entry = propertyIterator.next();
            if(CLASS_FIELD.equals(entry.getKey())) continue;
            Assert.assertEquals(beanMapDest.get(entry.getKey()), (entry.getValue()), String.format("Field value mismatch: %1$s", entry.getKey()));
        }
    }
}
