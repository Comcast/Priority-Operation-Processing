package com.theplatform.dfh.cp.endpoint.transformrequest.aws.persistence;

import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

public class PersistentTransformConverter extends DynamoDBPersistentObjectConverter<TransformRequest, PersistentTransform>
{
    public PersistentTransformConverter()
    {
        super(TransformRequest.class, PersistentTransform.class);
    }
}