package com.theplatform.dfh.cp.endpoint.transformrequest.aws.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.theplatform.dfh.cp.api.input.InputFileResource;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.endpoint.persistence.JsonDynamoDBTypeConverter;

import java.util.List;

public class ListInputFileResourceConverter extends JsonDynamoDBTypeConverter<List<InputFileResource>>
{
    public ListInputFileResourceConverter()
    {
        super(new TypeReference<List<InputFileResource>>(){});
    }
}
