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
    private final int QUERY_PAGE_SIZE = 100;

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

        // CRITICAL NOTE: DynamoDB does not have a clear concept of limited results.
        // this is the maximum number of items to evaluate from the DynamoDB table (per scan call)
        dynamoQueryExpression.setLimit(QUERY_PAGE_SIZE);

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
