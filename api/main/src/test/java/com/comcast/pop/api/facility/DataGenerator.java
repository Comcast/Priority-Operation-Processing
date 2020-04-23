package com.comcast.pop.api.facility;

public class DataGenerator
{
    public static ResourcePool generate()
    {
        ResourcePool resourcePool = generateResourcePool();
        Insight insight = generateInsight();
        resourcePool.addInsightId(insight.getId());
        return resourcePool;
    }
    public static ResourcePool generateResourcePool()
    {
        ResourcePool resourcePool = new ResourcePool();
        resourcePool.setId("2u9490283912832109283091");
        resourcePool.setCustomerId("my.resource.pool.user@mysite.com");
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
        customer.setTitle("test account");
        return customer;
    }
}
