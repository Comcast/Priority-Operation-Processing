package com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence;

import com.amazonaws.regions.Regions;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.comcast.fission.endpoint.api.data.query.resourcepool.insight.ByInsightId;
import com.comcast.fission.endpoint.api.data.query.scheduling.ByCustomerId;
import com.theplatform.dfh.object.api.UUIDGenerator;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.LocalDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;
import com.theplatform.dfh.scheduling.aws.persistence.PersistentReadyAgenda;
import com.theplatform.dfh.scheduling.aws.persistence.PersistentReadyAgendaConverter;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Local testing against live AWS
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
    public void testInsightLive()
    {
        final String TABLE_NAME = "Fission-Insight-dev";
        DynamoDBConvertedObjectPersister<Insight, PersistentInsight> persister = new DynamoDBConvertedObjectPersister<>(TABLE_NAME, "id",
            dynamoDBFactory, Insight.class, new PersistentInsightConverter(), DynamoDBInsightPersisterFactory.tableIndexes);

        final String id = new UUIDGenerator().generate();
        Insight insight = DataGenerator.generateInsight(id);
        insight.setId(id);
        persister.persist(insight);
        Insight retrievedInsight = persister.retrieve(id);
        Assert.assertNotNull(retrievedInsight);
        Assert.assertNotNull(retrievedInsight.getMappers());
    }

    @Test (enabled = false)
    public void testReadyAgendaLookup() throws Throwable
    {
        final String TABLE_NAME = "Fission-ReadyAgenda-dev";
        DynamoDBConvertedObjectPersister<ReadyAgenda, PersistentReadyAgenda> persister = new DynamoDBConvertedObjectPersister<>(
            TABLE_NAME,
            "id",
            dynamoDBFactory,
            ReadyAgenda.class,
            new PersistentReadyAgendaConverter(),
            new TableIndexes().withIndex("insightId_customerId_index", ByCustomerId.fieldName(), ByInsightId.fieldName())
            );

        DataObjectFeed<ReadyAgenda> feed = persister.retrieve(Arrays.asList(
            //new ByInsightId("insight"),
            //new ByCustomerId("customer")
        ));
        Assert.assertFalse(feed.isError());
        Assert.assertEquals(feed.getAll().size(), 1);

    }
}
