package com.theplatform.dfh.cp.endpoint.agenda.aws.persistence;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBCompressedObjectPersisterFactory;

public class DynamoDBAgendaPersisterFactory extends DynamoDBCompressedObjectPersisterFactory<Agenda>
{
    public DynamoDBAgendaPersisterFactory()
    {
        super("id", Agenda.class);
    }

    @Override
    public ObjectPersister getObjectPersister(String containerName)
    {
        return new DynamoDBAgendaPersister(containerName, persistenceKeyFieldName, new AWSDynamoDBFactory());
    }
}
