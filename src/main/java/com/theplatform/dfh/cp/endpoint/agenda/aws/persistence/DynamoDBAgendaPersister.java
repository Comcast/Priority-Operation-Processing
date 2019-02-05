package com.theplatform.dfh.cp.endpoint.agenda.aws.persistence;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBCompressedObjectPersister;

import java.util.Map;

public class DynamoDBAgendaPersister extends DynamoDBCompressedObjectPersister<Agenda>
{
    public final String JOB_ID_FIELD = "jobId";
    public final String CUSTOMER_ID_FIELD = "customerId";
    public final String CID_FIELD = "cid";

    public DynamoDBAgendaPersister(String tableName, String persistenceKeyFieldName, AWSDynamoDBFactory AWSDynamoDBFactory)
    {
        super(tableName, persistenceKeyFieldName, AWSDynamoDBFactory, Agenda.class);
    }

    @Override
    protected Map<String, AttributeValue> getStringAttributeValueMap(String identifier, Agenda object)
    {
        Map<String, AttributeValue> attributeMap = super.getStringAttributeValueMap(identifier, object);
        addNonNullStringAttribute(attributeMap, JOB_ID_FIELD, object.getJobId());
        addNonNullStringAttribute(attributeMap, CUSTOMER_ID_FIELD, object.getCustomerId());
        addNonNullStringAttribute(attributeMap, CID_FIELD, object.getCid());
        return attributeMap;
    }
}
