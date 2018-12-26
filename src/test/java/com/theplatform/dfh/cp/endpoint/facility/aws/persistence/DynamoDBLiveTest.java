package com.theplatform.dfh.cp.endpoint.facility.aws.persistence;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.object.api.IDGenerator;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedObjectPersister;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DynamoDBLiveTest
{
    AWSDynamoDBFactory dynamoDBFactory = Mockito.mock(AWSDynamoDBFactory.class);

    @Test(enabled = false)
    public void testInsightLive()
    {
        final String tableName = "DFH-Fission-Twinkle-Insight-dev";
        final String accessKey = "ASIAUNDOUIOXYO3HG36Z";
        final String secretKey = "nlE+qT3vbcyGdPHqmYDZMRncqFrj4AL8zzcNrG1r";
        final String sessionToken = "FQoGZXIvYXdzEIv//////////wEaDFugSJ2LKWbrq3SXoyKhApxjJ3SfCXnMH7Id52bapcBNs5Z3cpeNbMqz8qe9h2vnOqFyAVsMrlZJYswvrgELfUKEwQ4/p3Z4P1S4c1XEDda6im" +
            "/0ePXzchtrtqm6dwg5WMsxAqxrhTwz8aTdGaA66V/nbubw0LaEQ672g95WHVQKOBsvXFbwJCytC+mq/wcJ5D5niSstSczHYFxRvj42WLe73DtwZ9lWxWCF5du5N9AzGM7r8vql57XuQI2xw+4iQtbDgiRoiIJAQZV7BqQYFBhNnQaboIDzXDMqCFCj+wmzPwL0oBEiZKvADiHSN/lj5NpGP41zqTVudz4mahFHhV7KhlRX7TZyjB3mobhfhRbD86zI8f9agkUPSNuNms0IrRJbrEXt8l+NTIaXwpXucboopr3B4AU=";
        Mockito.when(dynamoDBFactory.getAmazonDynamoDB()).thenReturn(loadDB(accessKey, secretKey, sessionToken));
        DynamoDBConvertedObjectPersister<Insight> persister = new DynamoDBConvertedObjectPersister<Insight>(tableName, "id",
            dynamoDBFactory, Insight.class, new PersistentInsightConverter(), DynamoDBInsightPersisterFactory.tableIndexes);

        final String id = new IDGenerator().generate();
        Insight insight = DataGenerator.generateInsight(id);
        insight.setId(id);
        persister.persist(insight);
        Insight retrievedInsight = persister.retrieve(id);
        Assert.assertNotNull(retrievedInsight);
        Assert.assertNotNull(retrievedInsight.getMappers());
    }

    private AmazonDynamoDB loadDB(String accessKey, String secretKey, String token)
    {
        BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(accessKey, secretKey, token);
        return AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials))
                .withRegion(Regions.US_WEST_2)
                .build();
    }
}
