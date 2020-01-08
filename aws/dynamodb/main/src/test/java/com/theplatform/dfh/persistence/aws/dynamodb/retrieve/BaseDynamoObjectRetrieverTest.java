package com.theplatform.dfh.persistence.aws.dynamodb.retrieve;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.theplatform.dfh.persistence.api.field.IdField;
import com.theplatform.dfh.persistence.api.field.LimitField;
import com.theplatform.dfh.persistence.api.query.Query;
import com.theplatform.dfh.persistence.aws.dynamodb.QueryExpression;
import com.theplatform.dfh.persistence.aws.dynamodb.TestTrackedObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Test(s) for the retrievers
 */
public abstract class BaseDynamoObjectRetrieverTest
{
    protected DynamoDBMapper mockDynamoDBMapper;
    protected QueryExpression<TestTrackedObject> queryExpression;

    @BeforeMethod
    public void setup()
    {
        mockDynamoDBMapper = mock(DynamoDBMapper.class);
    }

    @Test
    public void testLimit()
    {
        final int LIMIT = 1;
        // both query and scan can use these (note there are no table indexes specified)
        queryExpression = new QueryExpression<>(null, Arrays.asList(
            new Query<>(new IdField(), "1234"),
            new Query<>(new LimitField(), LIMIT)
        ));
        // verify that even if there are more results only 1 is returned
        setupDynamoMapperMock(true, 10);
        List<TestTrackedObject> results = createObjectRetriever().retrieveObjects();
        Assert.assertEquals(results.size(), LIMIT);
    }

    protected abstract ObjectRetriever<TestTrackedObject> createObjectRetriever();

    protected abstract void setupDynamoMapperMock(boolean hasAdditionalPages, Integer items);
}
