package com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence;

import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.api.facility.SchedulingAlgorithm;
import com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper.OperationTypeMapper;
import com.theplatform.dfh.object.api.UUIDGenerator;

/**
 */
public class DataGenerator
{
    public static ResourcePool generate()
    {
        ResourcePool resourcePool = generateResourcePool();
        Insight insight = generateInsight();
        resourcePool.addInsight(insight);
        return resourcePool;
    }

    public static ResourcePool generateResourcePool()
    {
        ResourcePool resourcePool = new ResourcePool();
        resourcePool.setId("2u9490283912832109283091");
        resourcePool.setCustomerId("mpx/my.resource.pool.user@comcast.com");
        resourcePool.setTitle("CTS Seattle");
        return resourcePool;
    }

    public static Insight generateInsight()
    {
        return generateInsight(new UUIDGenerator().generate());
    }
    public static Insight generateInsight(String id)
    {
        Insight insight = new Insight();
        insight.setId(id);
        insight.setQueueName("aws:queue:my-queue");
        insight.setQueueSize(900);
        insight.setSchedulingAlgorithm(SchedulingAlgorithm.FirstInFirstOut);
        insight.addMapper(new OperationTypeMapper().withMatchValue("accelerate"));
        return insight;
    }

    public static Customer generateCustomer()
    {
        Customer customer = new Customer();
        customer.setBillingCode("My billing code");
        customer.setId("982048329048239jfkdsl");
        customer.setTitle("babs account");
        return customer;
    }
}
