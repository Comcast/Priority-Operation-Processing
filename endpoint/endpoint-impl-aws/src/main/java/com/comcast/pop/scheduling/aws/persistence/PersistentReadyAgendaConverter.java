package com.comcast.pop.scheduling.aws.persistence;

import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

/**
 * Specialized converter that persists additional field(s) on the persistent object that are not on the client object
 */
public class PersistentReadyAgendaConverter extends DynamoDBPersistentObjectConverter<ReadyAgenda, PersistentReadyAgenda>
{
    public PersistentReadyAgendaConverter()
    {
        super(ReadyAgenda.class, PersistentReadyAgenda.class);
    }

    @Override
    public PersistentReadyAgenda getPersistentObject(ReadyAgenda dataObject)
    {
        PersistentReadyAgenda persistentObject = super.getPersistentObject(dataObject);
        // create the composite fields (only visible to the table for the sake of dynamo indexing/sorting)
        if(null != persistentObject)
            persistentObject.setInsightIdCustomerIdComposite(persistentObject.getInsightId() + persistentObject.getCustomerId());
        return persistentObject;
    }
}
