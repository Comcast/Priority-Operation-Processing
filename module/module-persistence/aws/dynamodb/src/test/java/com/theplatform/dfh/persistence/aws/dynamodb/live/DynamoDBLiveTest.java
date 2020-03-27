package com.theplatform.dfh.persistence.aws.dynamodb.live;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.field.LimitField;
import com.theplatform.dfh.persistence.api.query.Query;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.LocalDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;
import com.theplatform.dfh.persistence.aws.dynamodb.TestTrackedObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Local testing against live AWS
 * http://tpconfluence.corp.theplatform.com/display/TD/Local+AWS+Testing+and+Java
 */

public class DynamoDBLiveTest
{
    private final String PROFILE_NAME = "lab_Fission";
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

        final String TABLE_NAME = "Fission-Agenda-dev";
        DynamoDBObjectPersister<TestTrackedObject> objectPersister = new DynamoDBObjectPersister<>(TABLE_NAME,
            "id", dynamoDBFactory, TestTrackedObject.class, tableIndexes);
        //Query<String> query = new Query<>("customerId", "http://access.auth.test.corp.theplatform.com/data/Account/3515465101");
        Query<String> query = new Query<>("linkId", "e9e29289-e3f2-47fe-a6c2-1500a9376732");

        Query<Integer> limitQuery = new Query<>(new LimitField(), 5);
        DataObjectFeed<TestTrackedObject> result = objectPersister.retrieve(Arrays.asList(query, limitQuery));
        System.out.println(new ObjectMapper().writeValueAsString(result));
    }
}
