package com.theplatform.dfh.cp.endpoint.transformrequest.aws.persistence;

import com.comcast.pop.api.TransformRequest;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

public class DynamoDBTransformPersisterFactory extends DynamoDBConvertedPersisterFactory<TransformRequest, PersistentTransform>
{
    private static final TableIndexes tableIndexes = new TableIndexes().withIndex("linkid_index", "linkId");

    public DynamoDBTransformPersisterFactory()
    {
        super("id", TransformRequest.class, new PersistentTransformConverter(), tableIndexes);
    }
}