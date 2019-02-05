package com.theplatform.dfh.cp.endpoint.transformrequest.aws.persistence;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBCompressedObjectPersister;

import java.util.Map;

public class DynamoDBTransformPersister extends DynamoDBCompressedObjectPersister<TransformRequest>
{
    public final String CUSTOMER_ID_FIELD = "customerId";
    public final String CID_FIELD = "cid";

    public DynamoDBTransformPersister(String tableName, String persistenceKeyFieldName, AWSDynamoDBFactory AWSDynamoDBFactory)
    {
        super(tableName, persistenceKeyFieldName, AWSDynamoDBFactory, TransformRequest.class);
    }

    @Override
    protected Map<String, AttributeValue> getStringAttributeValueMap(String identifier, TransformRequest object)
    {
        Map<String, AttributeValue> attributeMap = super.getStringAttributeValueMap(identifier, object);
        addNonNullStringAttribute(attributeMap, CUSTOMER_ID_FIELD, object.getCustomerId());
        addNonNullStringAttribute(attributeMap, CID_FIELD, object.getCid());
        return attributeMap;
    }
}