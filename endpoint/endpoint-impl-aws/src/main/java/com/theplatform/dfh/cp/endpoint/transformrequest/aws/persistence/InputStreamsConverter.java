package com.theplatform.dfh.cp.endpoint.transformrequest.aws.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.comcast.pop.api.input.InputStreams;
import com.comcast.pop.api.operation.Operation;
import com.theplatform.dfh.cp.endpoint.persistence.JsonDynamoDBTypeConverter;

import java.util.List;

public class InputStreamsConverter extends JsonDynamoDBTypeConverter<InputStreams>
{
    public InputStreamsConverter()
    {
        super(new TypeReference<InputStreams>(){});
    }
}
