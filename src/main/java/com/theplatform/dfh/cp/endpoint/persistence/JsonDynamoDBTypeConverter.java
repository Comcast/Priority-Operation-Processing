package com.theplatform.dfh.cp.endpoint.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonDynamoDBTypeConverter<T> implements DynamoDBTypeConverter<String, T>
{
    private static final Logger logger = LoggerFactory.getLogger(JsonDynamoDBTypeConverter.class);

    private static JsonHelper jsonHelper = new JsonHelper();

    private final TypeReference<T> typeReference;

    public JsonDynamoDBTypeConverter(TypeReference<T> typeReference)
    {
        this.typeReference = typeReference;
    }

    @Override
    public String convert(T object)
    {
        if(object ==null ) return null;
        return jsonHelper.getJSONString(object);
    }

    @Override
    public T unconvert(String s)
    {
        if(StringUtils.isBlank(s)) return null;
        try
        {
            return jsonHelper.getObjectMapper().readValue(s, typeReference);
        }
        catch(IOException e)
        {
            logger.error("Failed to convert type: " + typeReference.toString(), e);
            return null;
        }
    }
}
