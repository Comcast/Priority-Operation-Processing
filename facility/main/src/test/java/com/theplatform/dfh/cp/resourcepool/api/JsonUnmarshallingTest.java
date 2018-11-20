package com.theplatform.dfh.cp.resourcepool.api;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class JsonUnmarshallingTest
{

    @Test
    public void testJsonToSources() throws Exception
    {
        ResourcePool job = (ResourcePool)JsonUtil.toObject(getStringFromResourceFile("ResourcePool.json"), ResourcePool.class);
        Assert.assertNotNull(job);
    }

    protected String getStringFromResourceFile(String file) throws IOException
    {
        return IOUtils.toString(
                this.getClass().getResource(file),
                "UTF-8"
        );
    }
}
