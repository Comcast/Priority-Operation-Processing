package com.comcast.pop.cp.endpoint.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.comcast.pop.api.params.ParamsMap;

public class ParamsMapConverter extends JsonDynamoDBTypeConverter<ParamsMap>
{
    public ParamsMapConverter()
    {
        super(new TypeReference<ParamsMap>(){});
    }
}
