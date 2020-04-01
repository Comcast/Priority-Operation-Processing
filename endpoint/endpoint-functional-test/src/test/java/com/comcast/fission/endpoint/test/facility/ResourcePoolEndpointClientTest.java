package com.comcast.fission.endpoint.test.facility;

import com.comcast.fission.endpoint.test.BaseEndpointObjectClientTest;
import com.comcast.fission.endpoint.test.factory.DataGenerator;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

public class ResourcePoolEndpointClientTest extends BaseEndpointObjectClientTest<ResourcePool>
{
    private final String UPDATED_OWNER_ID = "theUpdate";
    private static final String CLASS_FIELD = "class";

    private static final Logger logger = LoggerFactory.getLogger(ResourcePoolEndpointClientTest.class);

    public ResourcePoolEndpointClientTest()
    {
        super(ResourcePool.class);
    }

    @Override
    public String getEndpointUrl()
    {
        return resourcePoolUrl;
    }

    @Override
    protected ResourcePool getTestObject()
    {
        ResourcePool resourcePool = DataGenerator.generateResourcePool(testCustomerId);
        return resourcePool;
    }

    @Override
    protected ResourcePool updateTestObject(ResourcePool object)
    {
        object.setTitle(UPDATED_OWNER_ID);
        return object;
    }

    @Override
    protected void verifyCreatedTestObject(ResourcePool createdObject, ResourcePool testObject)
    {
        assertEqual(createdObject, testObject);
    }

    @Override
    protected void verifyUpdatedTestObject(ResourcePool object)
    {
        Assert.assertEquals(object.getTitle(), UPDATED_OWNER_ID);
    }
}
