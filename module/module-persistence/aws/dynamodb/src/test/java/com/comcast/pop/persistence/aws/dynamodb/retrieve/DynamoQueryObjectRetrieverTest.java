package com.comcast.pop.persistence.aws.dynamodb.retrieve;

import com.comcast.pop.persistence.aws.dynamodb.TestTrackedObject;
import com.comcast.pop.persistence.aws.dynamodb.util.DynamoTestUtil;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

public class DynamoQueryObjectRetrieverTest extends BaseDynamoObjectRetrieverTest
{
    @Override
    protected ObjectRetriever<TestTrackedObject> createObjectRetriever()
    {
        return new DynamoQueryObjectRetriever<>(queryExpression, TestTrackedObject.class, mockDynamoDBMapper);
    }

    @Override
    protected void setupDynamoMapperMock(boolean hasAdditionalPages, Integer items)
    {
        doReturn(DynamoTestUtil.createQueryResultPage(hasAdditionalPages, items)).when(mockDynamoDBMapper).queryPage(any(), any());
    }
}
