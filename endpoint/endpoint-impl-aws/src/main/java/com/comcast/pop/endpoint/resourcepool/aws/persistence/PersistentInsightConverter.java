package com.comcast.pop.endpoint.resourcepool.aws.persistence;

import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;


public class PersistentInsightConverter extends DynamoDBPersistentObjectConverter<Insight, PersistentInsight>
{
    public PersistentInsightConverter()
    {
        super(Insight.class, PersistentInsight.class);
    }
}
