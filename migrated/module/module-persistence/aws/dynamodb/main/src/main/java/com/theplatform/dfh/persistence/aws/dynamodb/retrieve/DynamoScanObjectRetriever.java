package com.theplatform.dfh.persistence.aws.dynamodb.retrieve;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.ScanResultPage;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.field.LimitField;
import com.theplatform.dfh.persistence.aws.dynamodb.QueryExpression;

import java.util.LinkedList;
import java.util.List;

public class DynamoScanObjectRetriever<T extends IdentifiedObject> extends ObjectRetriever<T>
{
    private final int SCAN_LIMIT = 100;

    public DynamoScanObjectRetriever(QueryExpression<T> queryExpression, Class<T> dataObjectClass, DynamoDBMapper dynamoDBMapper)
    {
        super(queryExpression, dataObjectClass, dynamoDBMapper);
    }

    @Override
    public List<T> retrieveObjects()
    {
        List<T> retrievedObjects = new LinkedList<>();
        DynamoDBScanExpression dynamoScanExpression = queryExpression.forScan();

        // default the limit so a scan doesn't get the entire table
        final int LIMIT = queryExpression.getLimit() == null ? LimitField.defaultValue() : queryExpression.getLimit();

        // CRITICAL NOTE: DynamoDB does not have a concept of limited results.
        // this is the maximum number of items to evaluate from the DynamoDB table (per scan call)
        dynamoScanExpression.setLimit(SCAN_LIMIT);

        ScanResultPage<T> scanResultPage;
        do
        {
            scanResultPage = dynamoDBMapper.scanPage(dataObjectClass, dynamoScanExpression);
            retrievedObjects.addAll(scanResultPage.getResults());
            dynamoScanExpression.setExclusiveStartKey(scanResultPage.getLastEvaluatedKey());
        }while(
            scanResultPage.getLastEvaluatedKey() != null
            && retrievedObjects.size() < LIMIT
        );
        return retrievedObjects.subList(0, Math.min(retrievedObjects.size(), LIMIT));
    }
}