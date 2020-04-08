package com.comcast.pop.cp.endpoint.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.comcast.pop.api.progress.DiagnosticEvent;

public class DiagnosticEventArrayConverter extends JsonDynamoDBTypeConverter<DiagnosticEvent[]>
{
    public DiagnosticEventArrayConverter()
    {
        super(new TypeReference<DiagnosticEvent[]>(){});
    }
}
