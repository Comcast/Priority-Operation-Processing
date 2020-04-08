package com.comcast.pop.persistence.aws.dynamodb.retrieve;

import com.comcast.pop.persistence.aws.dynamodb.TestTrackedObject;
import com.comcast.pop.persistence.aws.dynamodb.util.DynamoTestUtil;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

public class DynamoScanObjectRetrieverTest extends BaseDynamoObjectRetrieverTest
{
    @Override
    protected ObjectRetriever<TestTrackedObject> createObjectRetriever()
    {
        return new DynamoScanObjectRetriever<>(queryExpression, TestTrackedObject.class, mockDynamoDBMapper);
    }

    @Override
    protected void setupDynamoMapperMock(boolean hasAdditionalPages, Integer items)
    {
        doReturn(DynamoTestUtil.createScanResultPage(hasAdditionalPages, items)).when(mockDynamoDBMapper).scanPage(any(), any());
    }
}
