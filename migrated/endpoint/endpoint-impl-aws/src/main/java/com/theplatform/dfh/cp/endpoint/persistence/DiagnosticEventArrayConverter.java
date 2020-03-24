package com.theplatform.dfh.cp.endpoint.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.endpoint.persistence.JsonDynamoDBTypeConverter;

public class DiagnosticEventArrayConverter extends JsonDynamoDBTypeConverter<DiagnosticEvent[]>
{
    public DiagnosticEventArrayConverter()
    {
        super(new TypeReference<DiagnosticEvent[]>(){});
    }
}
