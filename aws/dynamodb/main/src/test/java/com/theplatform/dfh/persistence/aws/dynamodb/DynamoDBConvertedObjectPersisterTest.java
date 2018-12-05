package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
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
    DynamoDBConvertedObjectPersister persister = new DynamoDBConvertedObjectPersister("table", "id", awsDynamoDBFactory, MyTest.class, mockPersistentObjectConverter);


    @Test(expectedExceptions = PersistenceException.class)
    public void testQueryException() throws PersistenceException
    {
        persister.setDynamoDBMapper(dbMapper);
        Mockito.doReturn(PersistentMyTest.class).when(mockPersistentObjectConverter).getPersistentObjectClass();
        Mockito.when(dbMapper.query(Mockito.eq(PersistentMyTest.class), Mockito.any())).thenThrow(new AmazonDynamoDBException("bad params"));
        DataObjectFeed<MyTest> returnedFeed = persister.retrieve(Collections.singletonList(new Query("id","xyz")));
    }

    private class MyTest
    {
        private String id;
        private String title;

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }
    }

    private class PersistentMyTest extends MyTest
    {
    }
}
