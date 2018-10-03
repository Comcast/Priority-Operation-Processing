package com.theplatform.dfh.cp.facility.api;

import com.theplatform.dfh.cp.facility.api.Facility;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class JsonUnmarshallingTest
{

    @Test
    public void testJsonToSources() throws Exception
    {
        Facility job = (Facility)JsonUtil.toObject(getStringFromResourceFile("Job.json"), Facility.class);
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
