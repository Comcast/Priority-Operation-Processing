package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.theplatform.dfh.persistence.api.query.ByTitle;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

public class QueryExpressionTest
{
    QueryExpression queryExpression = new QueryExpression();

    @Test
    public void testNull()
    {
        Assert.assertNull(queryExpression.from(null));
    }
    @Test
    public void testByTitle()
    {
        DynamoDBQueryExpression expression = queryExpression.from(Collections.singletonList(new ByTitle("xyz")));
        Assert.assertEquals(expression.getKeyConditionExpression(), "title = :title");
        Assert.assertTrue(expression.getExpressionAttributeValues().get(":title").toString().contains("xyz"));
    }
}
