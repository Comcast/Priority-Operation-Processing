package com.comcast.pop.cp.endpoint.resourcepool.aws.persistence;

import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.comcast.pop.persistence.aws.dynamodb.TableIndexes;

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
