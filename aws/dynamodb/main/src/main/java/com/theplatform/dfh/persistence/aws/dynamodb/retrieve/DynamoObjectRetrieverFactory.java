package com.theplatform.dfh.persistence.aws.dynamodb.retrieve;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.aws.dynamodb.QueryExpression;

public class DynamoObjectRetrieverFactory<T extends IdentifiedObject>
{
    public ObjectRetriever<T> createObjectRetriever(QueryExpression<T> queryExpression, Class<T> dataObjectClass, DynamoDBMapper dynamoDBMapper)
    {
        if(queryExpression.hasKey())
            return new DynamoQueryObjectRetriever<>(queryExpression, dataObjectClass, dynamoDBMapper);
        return new DynamoScanObjectRetriever<>(queryExpression, dataObjectClass, dynamoDBMapper);
    }
}
