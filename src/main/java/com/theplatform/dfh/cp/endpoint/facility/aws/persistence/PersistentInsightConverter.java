package com.theplatform.dfh.cp.endpoint.facility.aws.persistence;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;


public class PersistentInsightConverter extends DynamoDBPersistentObjectConverter<Insight, PersistentInsight>
{
    public PersistentInsightConverter()
    {
        super(Insight.class, PersistentInsight.class);
    }
}
