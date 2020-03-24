package com.theplatform.dfh.cp.endpoint.agenda.aws.persistence;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

public class DynamoDBAgendaPersisterFactory extends DynamoDBConvertedPersisterFactory<Agenda, PersistentAgenda>
{
    private static final TableIndexes tableIndexes = new TableIndexes().withIndex("linkid_index", "linkId");

    public DynamoDBAgendaPersisterFactory()
    {
        super("id", Agenda.class, new PersistentAgendaConverter(), tableIndexes);
    }
}
