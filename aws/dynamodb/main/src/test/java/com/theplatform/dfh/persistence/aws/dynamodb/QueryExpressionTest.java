package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.theplatform.dfh.persistence.api.query.Query;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

public class QueryExpressionTest
{
    QueryExpression queryExpression = new QueryExpression(null);

    @Test
    public void testByTitle()
    {
        DynamoDBQueryExpression expression = queryExpression.forQuery(Collections.singletonList(new Query("title","xyz")));
        Assert.assertEquals(expression.getKeyConditionExpression(), "title = :title");
        Assert.assertTrue(expression.getExpressionAttributeValues().get(":title").toString().contains("xyz"));
    }

    @Test
    public void testAll()
    {
        DynamoDBQueryExpression expression = queryExpression.forQuery(null);
        Assert.assertNull(expression);
    }
}
