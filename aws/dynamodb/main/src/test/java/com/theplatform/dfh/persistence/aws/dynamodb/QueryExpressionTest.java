package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.theplatform.dfh.persistence.api.query.Query;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

public class QueryExpressionTest
{
    @Test
    public void testPrimaryKey()
    {
        QueryExpression queryExpression = new QueryExpression(null, Collections.singletonList(new Query("id","xyz")));
        DynamoDBQueryExpression expression = queryExpression.forQuery();
        Assert.assertEquals(expression.getKeyConditionExpression(), "id = :id");
        Assert.assertTrue(expression.getExpressionAttributeValues().get(":id").toString().contains("xyz"));
    }
    @Test
    public void testPrimaryKeyWithByTitle()
    {
        QueryExpression queryExpression = new QueryExpression(null, Arrays.asList(new Query("id", "123"), new Query("title","xyz")));
        DynamoDBQueryExpression expression = queryExpression.forQuery();
        Assert.assertEquals(expression.getKeyConditionExpression(), "id = :id");
        Assert.assertTrue(expression.getExpressionAttributeValues().get(":title").toString().contains("xyz"));
        Assert.assertTrue(expression.getExpressionAttributeValues().get(":id").toString().contains("123"));
    }

    @Test
    public void test()
    {
        QueryExpression queryExpression = new QueryExpression(null, null);
        DynamoDBQueryExpression expression = queryExpression.forQuery();
        Assert.assertNull(expression);
    }
}
