package com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

/**
 */
public class DynamoDBInsightPersisterFactory extends DynamoDBConvertedPersisterFactory<Insight, PersistentInsight>
{
    protected static final TableIndexes tableIndexes = new TableIndexes().withIndex("resourcepoolid_index", "resourcePoolId");

    public DynamoDBInsightPersisterFactory()
    {
        super("id", Insight.class, new PersistentInsightConverter(), tableIndexes);
    }
}
