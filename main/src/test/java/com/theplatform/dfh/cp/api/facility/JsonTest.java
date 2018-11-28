package com.theplatform.dfh.cp.api.facility;

import com.theplatform.dfh.cp.api.JsonUtil;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class JsonTest
{
    @Test
    public void testJsonToResourcePool() throws Exception
    {
        ResourcePool job = (ResourcePool) JsonUtil.toObject(getStringFromResourceFile("ResourcePool.json"), ResourcePool.class);
        Assert.assertNotNull(job);
        Assert.assertEquals(job.getId(),"2u9490283912832109283091");
        Assert.assertNotNull(job.getInsights());
        Assert.assertEquals("9384932rfdiofjwoiejf",job.getInsights().get(0).getId());
    }
    @Test
    public void testResourcePoolToJson() throws Exception
    {
        ResourcePool resourcePool = DataGenerator.generate();
        String json = JsonUtil.toJson(resourcePool);
        //System.out.println(json);
        Assert.assertNotNull(json);
    }
    @Test
    public void testCustomerToJson() throws Exception
    {
        Customer customer = DataGenerator.generateCustomer();
        ResourcePool resourcePool = DataGenerator.generateResourcePool();
        customer.addAvailableResourcePool(resourcePool);
        String json = JsonUtil.toJson(customer);
        //System.out.println(json);
        Assert.assertNotNull(json);
    }
    protected String getStringFromResourceFile(String file) throws IOException
    {
        return IOUtils.toString(
                this.getClass().getResource(file),
                "UTF-8"
        );
    }
}
