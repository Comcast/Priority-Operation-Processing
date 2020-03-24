package com.theplatform.dfh.cp.endpoint.progress.aws.persistence;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.query.Query;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class DynamoDBPersistenceTest
{
    private static final List<Query> idAndFilter = Arrays.asList(new Query("id", "d09921a1-e1b7-410a-b3bd-5957e6c26d3b"), new Query("processingState","WAITING"));
    private static final List<Query> indexAndFilter = Arrays.asList(new Query("linkId", "10ce2d72-7fae-4f6b-8330-bfdb529f509f"), new Query("processingState","WAITING"), new Query("limit", 2));
    private static final List<Query> scan = Arrays.asList(new Query("processingState","WAITING"), new Query("limit", 2));
    @Test(enabled = false)
    public void testLiveDynamoDBGetAll() throws PersistenceException
    {
        final String persistenceKeyField = "id";
        final String tableName = "DFH-Purple-Fission-ProgressAgenda-dev";
        ObjectPersister persister = new DynamoDBAgendaProgressPersisterFactory().getObjectPersister(tableName);;
        DataObjectFeed<AgendaProgress> response = persister.retrieve(scan);
        Assert.assertFalse(response.isError());
    }

    private class LiveAwsDynamoDBFactory extends AWSDynamoDBFactory
    {
        BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
                "ASIAUNDOUIOXTA3YLBFY", // access key id
                "mlvygzfr2qnoRD4WnWj+6zgUgZ8BCWgH/A5MEwiu", // secret access
                "FQoGZXIvYXdzECoaDJITWA8R0Z/8eY8OsCKhAmN5eTFgU4JTKoVQGdxxf7IjnZo7Mc/0Ulj3d+wdIOCgVSUK8xmXcQjFIRrOMY79lptrQ6B2PeTt+pKoUw1Bvez2o+AHBu9nrJZcW1kqREpKYw93/YbRK0yVldAnV1CfSJd+s5DpLs4l0F/2lO6yGXoSS75XW2Q8vBJkp5PVbn/foT2Ve7g5QogAYTsHSm3Y5ETPKb2FIB3NgDztzWU5uu22O18gknO9kv0OdFG2uG2FLuvJXItUFMojwaz/+gzBfDgxSgYFUTZvIi0HYimpzjV0WZecv2tPuBcLL0Xt/ZHFGzNZSRy8xPDOacjCC6Fo7orF8QnjGSYpuE4h9KvmK7f3CMhAYpXwHXkpUD1Iq82l92ULYQZzHWfbCDzm/KYclkoo2rCs4AU=" // session token
        );

        public AmazonDynamoDB getAmazonDynamoDB()
        {
            return AmazonDynamoDBClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials))
                    .withRegion(Regions.US_WEST_2)
                    .build();
        }
    }

}
