package com.comcast.pop.scheduling.aws.persistence;

import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.data.query.scheduling.ByCustomerId;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.comcast.pop.persistence.aws.dynamodb.TableIndexes;

public class DynamoDbReadyAgendaPersisterFactory extends DynamoDBConvertedPersisterFactory<ReadyAgenda, PersistentReadyAgenda>
{
    private static final TableIndexes tableIndexes = new TableIndexes().withIndex("customerId_index", ByCustomerId.fieldName());

    public DynamoDbReadyAgendaPersisterFactory()
    {
        super("id", ReadyAgenda.class, new PersistentReadyAgendaConverter(), tableIndexes);
    }
}
