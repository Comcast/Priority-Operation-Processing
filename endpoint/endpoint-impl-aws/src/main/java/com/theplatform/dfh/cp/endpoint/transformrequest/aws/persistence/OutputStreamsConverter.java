package com.theplatform.dfh.cp.endpoint.transformrequest.aws.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.output.OutputStreams;
import com.theplatform.dfh.cp.endpoint.persistence.JsonDynamoDBTypeConverter;

import java.util.List;

public class OutputStreamsConverter extends JsonDynamoDBTypeConverter<OutputStreams>
{
    public OutputStreamsConverter()
    {
        super(new TypeReference<OutputStreams>(){});
    }
}
