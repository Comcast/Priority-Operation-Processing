package com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence;

import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.facility.ResourcePool;
import com.comcast.pop.api.facility.SchedulingAlgorithm;
import com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper.OperationTypeMapper;
import com.comcast.pop.object.api.UUIDGenerator;

/**
 */
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
        resourcePool.setCustomerId("my.resource.pool.user@site.com");
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
}
