package com.theplatform.dfh.cp.endpoint.agenda.aws.persistence;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.endpoint.progress.aws.persistence.PersistentAgendaProgressConverter;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBCompressedObjectPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

public class DynamoDBAgendaPersisterFactory extends DynamoDBConvertedPersisterFactory<Agenda>
{
    private static final TableIndexes tableIndexes = null; // coming soon

    public DynamoDBAgendaPersisterFactory()
    {
        super("id", Agenda.class, new PersistentAgendaConverter(), tableIndexes);
    }
}
