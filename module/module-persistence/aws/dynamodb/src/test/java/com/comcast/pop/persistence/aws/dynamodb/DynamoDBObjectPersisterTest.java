package com.comcast.pop.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.comcast.pop.persistence.api.DataObjectFeed;
import com.comcast.pop.persistence.api.PersistenceException;
import com.comcast.pop.persistence.api.query.Query;
import com.comcast.pop.persistence.aws.dynamodb.util.DynamoTestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class DynamoDBObjectPersisterTest
{
    private DynamoDBObjectPersister<TestTrackedObject> persister;
    private AWSDynamoDBFactory mockAWSDynamoDBFactory;
    private DynamoDBMapper mockDynamoDBMapper;

    @BeforeMethod
    public void setup()
    {
        mockAWSDynamoDBFactory = mock(AWSDynamoDBFactory.class);
        mockDynamoDBMapper = mock(DynamoDBMapper.class);

        persister = new DynamoDBObjectPersister<>("", "id", mockAWSDynamoDBFactory, TestTrackedObject.class, mockDynamoDBMapper, null);
    }

    @Test
    public void testQueryAsScan() throws PersistenceException
    {
        final Integer ITEM_COUNT = 1;
        doReturn(DynamoTestUtil.createScanResultPage(false, ITEM_COUNT)).when(mockDynamoDBMapper).scanPage(any(), any());
        DataObjectFeed<TestTrackedObject> feed = persister.query(null);
        Assert.assertNotNull(feed);
        Assert.assertEquals(feed.getAll().size(), ITEM_COUNT.intValue());
    }

    @Test
    public void testQueryAsQuery() throws PersistenceException
    {
        final Integer ITEM_COUNT = 1;
        doReturn(DynamoTestUtil.createQueryResultPage(false, ITEM_COUNT)).when(mockDynamoDBMapper).queryPage(any(), any());
        DataObjectFeed<TestTrackedObject> feed = persister.query(Collections.singletonList(new Query<>("id", "1234")));
        Assert.assertNotNull(feed);
        Assert.assertEquals(feed.getAll().size(), ITEM_COUNT.intValue());
    }

}
