package com.theplatform.dfh.cp.endpoint.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.apache.commons.lang.StringUtils;

public class ParamsMapConverter implements DynamoDBTypeConverter<String, ParamsMap>
{
    static JsonHelper jsonHelper = new JsonHelper();
    @Override
    public String convert(ParamsMap paramsMap)
    {
        if(paramsMap == null) return null;
        return jsonHelper.getJSONString(paramsMap);
    }

    @Override
    public ParamsMap unconvert(String s)
    {
        if(StringUtils.isBlank(s)) return null;
        return jsonHelper.getObjectFromString(s, ParamsMap.class);
    }
}
