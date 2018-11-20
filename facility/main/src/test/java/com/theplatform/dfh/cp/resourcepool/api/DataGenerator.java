package com.theplatform.dfh.cp.resourcepool.api;

public class DataGenerator
{
    public ResourcePool generateResourcePool()
    {
        ResourcePool resourcePool = new ResourcePool();
        resourcePool.setId("2u9490283912832109283091");
        resourcePool.setOwnerId("mpx/my.resource.pool.user@comcast.com");
        resourcePool.setTitle("CTS Seattle");
        resourcePool.addInsight(generateInsight());
        return resourcePool;
    }
    public Insight generateInsight()
    {
        Insight insight = new Insight();
        insight.setId("9384932rfdiofjwoiejf");
        insight.setQueueName("aws:queue:my-queue");
        insight.setQueueSize(900);
        insight.setSchedulingAlgorithm(SchedulingAlgorithm.FirstInFirstOut);
        return insight;
    }
}
