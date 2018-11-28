package com.theplatform.dfh.persistence.aws.dynamodb.api.resourcepool;

import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.api.facility.SchedulingAlgorithm;

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
        resourcePool.setOwnerId("mpx/my.resource.pool.user@comcast.com");
        resourcePool.setTitle("CTS Seattle");
        return resourcePool;
    }

    public static Insight generateInsight()
    {
        Insight insight = new Insight();
        insight.setId("9384932rfdiofjwoiejf");
        insight.setQueueName("aws:queue:my-queue");
        insight.setQueueSize(900);
        insight.setSchedulingAlgorithm(SchedulingAlgorithm.FirstInFirstOut);
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
