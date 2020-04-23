package com.comcast.pop.endpoint.agenda.aws.persistence;

import com.comcast.pop.endpoint.persistence.JsonDynamoDBTypeConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.comcast.pop.api.operation.Operation;

import java.util.List;

public class ListOperationsConverter extends JsonDynamoDBTypeConverter<List<Operation>>
{
    public ListOperationsConverter()
    {
        super(new TypeReference<List<Operation>>(){});
    }
}
