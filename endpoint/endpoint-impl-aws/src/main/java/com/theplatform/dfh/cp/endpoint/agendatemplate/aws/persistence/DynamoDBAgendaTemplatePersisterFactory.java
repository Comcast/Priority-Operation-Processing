package com.theplatform.dfh.cp.endpoint.agendatemplate.aws.persistence;

import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

public class DynamoDBAgendaTemplatePersisterFactory extends DynamoDBConvertedPersisterFactory<AgendaTemplate, PersistentAgendaTemplate>
{
    private static final TableIndexes tableIndexes = new TableIndexes().withIndex("title_index", "title");

    public DynamoDBAgendaTemplatePersisterFactory()
    {
        super("id", AgendaTemplate.class, new PersistentAgendaTemplateConverter(), tableIndexes);
    }
}
