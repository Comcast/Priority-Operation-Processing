package com.theplatform.dfh.cp.endpoint.agenda.aws.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.comcast.pop.api.operation.Operation;
import com.theplatform.dfh.cp.endpoint.persistence.JsonDynamoDBTypeConverter;

import java.util.List;

public class ListOperationsConverter extends JsonDynamoDBTypeConverter<List<Operation>>
{
    public ListOperationsConverter()
    {
        super(new TypeReference<List<Operation>>(){});
    }
}
