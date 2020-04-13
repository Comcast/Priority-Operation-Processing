package com.comcast.pop.endpoint.agenda.aws.persistence;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.comcast.pop.persistence.aws.dynamodb.TableIndexes;

public class DynamoDBAgendaPersisterFactory extends DynamoDBConvertedPersisterFactory<Agenda, PersistentAgenda>
{
    private static final TableIndexes tableIndexes = new TableIndexes().withIndex("linkid_index", "linkId");

    public DynamoDBAgendaPersisterFactory()
    {
        super("id", Agenda.class, new PersistentAgendaConverter(), tableIndexes);
    }
}
