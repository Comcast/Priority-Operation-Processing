package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.PersistentObjectConverter;
import com.theplatform.dfh.persistence.api.query.Query;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 */
public class DynamoDBConvertedObjectPersisterTest
{
    AWSDynamoDBFactory awsDynamoDBFactory = Mockito.mock(AWSDynamoDBFactory.class);
    DynamoDBMapper dbMapper = Mockito.mock(DynamoDBMapper.class);
    PersistentObjectConverter<MyTest, PersistentMyTest> mockPersistentObjectConverter = Mockito.mock(PersistentObjectConverter.class);
    DynamoDBConvertedObjectPersister<MyTest, PersistentMyTest> persister =
        new DynamoDBConvertedObjectPersister<>("table", "id", awsDynamoDBFactory,
            MyTest.class, mockPersistentObjectConverter, null);


    @Test(expectedExceptions = PersistenceException.class)
    public void testQueryException() throws PersistenceException
    {
        persister.setDynamoDBMapper(dbMapper);
        Mockito.doReturn(PersistentMyTest.class).when(mockPersistentObjectConverter).getPersistentObjectClass();
        Mockito.when(dbMapper.queryPage(Mockito.eq(PersistentMyTest.class), Mockito.any())).thenThrow(new AmazonDynamoDBException("bad params"));
        DataObjectFeed<MyTest> returnedFeed = persister.retrieve(Collections.singletonList(new Query<>("id","xyz")));
    }

    private class MyTest implements IdentifiedObject
    {
        private String id;
        private String title;
        private String customerId;

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        @Override
        public String getCustomerId()
        {
            return customerId;
        }

        @Override
        public void setCustomerId(String s)
        {
            customerId = s;
        }
    }

    private class PersistentMyTest extends MyTest
    {
    }
}
