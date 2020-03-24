package com.theplatform.dfh.scheduling.aws.persistence;

import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.data.query.scheduling.ByCustomerId;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

public class DynamoDbReadyAgendaPersisterFactory extends DynamoDBConvertedPersisterFactory<ReadyAgenda, PersistentReadyAgenda>
{
    private static final TableIndexes tableIndexes = new TableIndexes().withIndex("customerId_index", ByCustomerId.fieldName());

    public DynamoDbReadyAgendaPersisterFactory()
    {
        super("id", ReadyAgenda.class, new PersistentReadyAgendaConverter(), tableIndexes);
    }
}
