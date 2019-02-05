package com.theplatform.dfh.cp.endpoint.transformrequest.aws.persistence;

import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBCompressedObjectPersisterFactory;

public class DynamoDBTransformPersisterFactory extends DynamoDBCompressedObjectPersisterFactory<TransformRequest>
{
    public DynamoDBTransformPersisterFactory()
    {
        super("id", TransformRequest.class);
    }

    @Override
    public ObjectPersister getObjectPersister(String containerName)
    {
        return new DynamoDBTransformPersister(containerName, persistenceKeyFieldName, new AWSDynamoDBFactory());
    }
}
