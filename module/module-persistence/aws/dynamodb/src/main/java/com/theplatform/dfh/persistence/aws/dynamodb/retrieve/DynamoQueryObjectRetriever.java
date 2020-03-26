package com.theplatform.dfh.persistence.aws.dynamodb.retrieve;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.field.LimitField;
import com.theplatform.dfh.persistence.aws.dynamodb.QueryExpression;

import java.util.LinkedList;
import java.util.List;

public class DynamoQueryObjectRetriever<T extends IdentifiedObject> extends ObjectRetriever<T>
{
    public DynamoQueryObjectRetriever(QueryExpression<T> queryExpression, Class<T> dataObjectClass, DynamoDBMapper dynamoDBMapper)
    {
        super(queryExpression, dataObjectClass, dynamoDBMapper);
    }

    @Override
    public List<T> retrieveObjects()
    {
        List<T> retrievedObjects = new LinkedList<>();
        DynamoDBQueryExpression<T> dynamoQueryExpression = queryExpression.forQuery();
        if (dynamoQueryExpression == null)
            return retrievedObjects;
        // default the limit so a query doesn't get the entire table
        final int LIMIT = queryExpression.getLimit() == null ? LimitField.defaultValue() : queryExpression.getLimit();

        // Note: this uses the default query limit (as query limit has little to do with the actual resulting number of items)

        QueryResultPage<T> queryResultPage;
        do
        {
            queryResultPage = dynamoDBMapper.queryPage(dataObjectClass, dynamoQueryExpression);
            retrievedObjects.addAll(queryResultPage.getResults());
            dynamoQueryExpression.setExclusiveStartKey(queryResultPage.getLastEvaluatedKey());
        }while(
            queryResultPage.getLastEvaluatedKey() != null
                && retrievedObjects.size() < LIMIT
        );
        return retrievedObjects.subList(0, Math.min(retrievedObjects.size(), LIMIT));
    }
}
