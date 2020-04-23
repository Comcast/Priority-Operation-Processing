package com.comcast.pop.endpoint.agendatemplate.aws.persistence;

import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.comcast.pop.persistence.aws.dynamodb.TableIndexes;

public class DynamoDBAgendaTemplatePersisterFactory extends DynamoDBConvertedPersisterFactory<AgendaTemplate, PersistentAgendaTemplate>
{
    private static final TableIndexes tableIndexes = new TableIndexes().withIndex("title_index", "title");

    public DynamoDBAgendaTemplatePersisterFactory()
    {
        super("id", AgendaTemplate.class, new PersistentAgendaTemplateConverter(), tableIndexes);
    }
}
