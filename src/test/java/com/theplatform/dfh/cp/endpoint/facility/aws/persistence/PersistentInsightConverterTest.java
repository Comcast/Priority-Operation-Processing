package com.theplatform.dfh.cp.endpoint.facility.aws.persistence;

import com.theplatform.dfh.cp.api.facility.Insight;
import org.testng.Assert;
import org.testng.annotations.Test;


public class PersistentInsightConverterTest
{
    private PersistentInsightConverter converter = new PersistentInsightConverter();
    @Test
    public void testConvertToPersistence()
    {
        Insight dataObject = DataGenerator.generateInsight();
        PersistentInsight persistentObject = converter.getPersistentObject(dataObject);
        Assert.assertNotNull(persistentObject.getMappers());
    }
}
