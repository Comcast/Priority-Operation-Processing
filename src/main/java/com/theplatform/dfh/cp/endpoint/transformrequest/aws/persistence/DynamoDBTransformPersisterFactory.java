package com.theplatform.dfh.cp.endpoint.transformrequest.aws.persistence;

import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

public class DynamoDBTransformPersisterFactory extends DynamoDBConvertedPersisterFactory<TransformRequest>
{
    private static final TableIndexes tableIndexes = null; // coming soon

    public DynamoDBTransformPersisterFactory()
    {
        super("id", TransformRequest.class, new PersistentTransformConverter(), tableIndexes);
    }
}