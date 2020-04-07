package com.theplatform.dfh.cp.endpoint.transformrequest.aws.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.comcast.pop.api.input.InputFileResource;
import com.comcast.pop.api.operation.Operation;
import com.theplatform.dfh.cp.endpoint.persistence.JsonDynamoDBTypeConverter;

import java.util.List;

public class ListInputFileResourceConverter extends JsonDynamoDBTypeConverter<List<InputFileResource>>
{
    public ListInputFileResourceConverter()
    {
        super(new TypeReference<List<InputFileResource>>(){});
    }
}
