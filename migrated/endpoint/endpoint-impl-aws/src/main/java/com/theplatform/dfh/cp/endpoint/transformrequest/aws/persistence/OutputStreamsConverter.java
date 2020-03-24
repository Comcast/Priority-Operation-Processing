package com.theplatform.dfh.cp.endpoint.transformrequest.aws.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.output.OutputStreams;
import com.theplatform.dfh.cp.endpoint.persistence.JsonDynamoDBTypeConverter;

import java.util.List;

public class OutputStreamsConverter extends JsonDynamoDBTypeConverter<OutputStreams>
{
    public OutputStreamsConverter()
    {
        super(new TypeReference<OutputStreams>(){});
    }
}
