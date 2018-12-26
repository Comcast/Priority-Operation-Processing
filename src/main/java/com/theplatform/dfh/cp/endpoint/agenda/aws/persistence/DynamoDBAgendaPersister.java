package com.theplatform.dfh.cp.endpoint.agenda.aws.persistence;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBCompressedObjectPersister;

import java.util.Map;

public class DynamoDBAgendaPersister extends DynamoDBCompressedObjectPersister<Agenda>
{
    public final String JOB_ID_FIELD = "jobId";

    public DynamoDBAgendaPersister(String tableName, String persistenceKeyFieldName, AWSDynamoDBFactory AWSDynamoDBFactory)
    {
        super(tableName, persistenceKeyFieldName, AWSDynamoDBFactory, Agenda.class);
    }

    @Override
    protected Map<String, AttributeValue> getStringAttributeValueMap(String identifier, Agenda object)
    {
        Map<String, AttributeValue> attributeMap = super.getStringAttributeValueMap(identifier, object);
        attributeMap.put(JOB_ID_FIELD, new AttributeValue().withS(object.getJobId()));
        return attributeMap;
    }
}
