package com.comcast.pop.persistence.aws.dynamodb.live;

import com.amazonaws.regions.Regions;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBObjectPersister;
import com.comcast.pop.persistence.aws.dynamodb.LocalDynamoDBFactory;
import com.comcast.pop.persistence.aws.dynamodb.TableIndexes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.comcast.pop.persistence.api.DataObjectFeed;
import com.comcast.pop.persistence.api.PersistenceException;
import com.comcast.pop.persistence.api.field.LimitField;
import com.comcast.pop.persistence.api.query.Query;
import com.comcast.pop.persistence.aws.dynamodb.TestTrackedObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Local testing against live AWS
 */

public class DynamoDBLiveTest
{
    private final String PROFILE_NAME = "lab_POP";
    private LocalDynamoDBFactory dynamoDBFactory;

    @BeforeMethod
    public void setup()
    {
        dynamoDBFactory = new LocalDynamoDBFactory(PROFILE_NAME, Regions.US_WEST_2);
    }

    @Test(enabled = false)
    public void testInsightLive() throws Exception, PersistenceException
    {
        final TableIndexes tableIndexes = new TableIndexes().withIndex("linkid_index", "linkId");

        final String TABLE_NAME = "POP-Agenda-dev";
        DynamoDBObjectPersister<TestTrackedObject> objectPersister = new DynamoDBObjectPersister<>(TABLE_NAME,
            "id", dynamoDBFactory, TestTrackedObject.class, tableIndexes);
        //Query<String> query = new Query<>("customerId", "http://access.auth.test.corp.theplatform.com/data/Account/3515465101");
        Query<String> query = new Query<>("linkId", "e9e29289-e3f2-47fe-a6c2-1500a9376732");

        Query<Integer> limitQuery = new Query<>(new LimitField(), 5);
        DataObjectFeed<TestTrackedObject> result = objectPersister.retrieve(Arrays.asList(query, limitQuery));
        System.out.println(new ObjectMapper().writeValueAsString(result));
    }
}
