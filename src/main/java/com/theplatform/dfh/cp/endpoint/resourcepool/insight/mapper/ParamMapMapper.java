package com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.ParamsMap;

import java.util.List;
import java.util.Map;

public class ParamMapMapper extends AbstractInsightKeyValueMapper
{
    public ParamMapMapper()
    {
        super(InsightMapperRegistry.paramMap.name());
    }

    @Override
    public boolean matches(Agenda agenda)
    {
        if(agenda == null) return false;
        //top level param map
        ParamsMap paramMap = agenda.getParams();
        if(matches(paramMap)) return true;

        List<Operation> operations = agenda.getOperations();
        if(operations == null || operations.size() == 0) return false;

        return operations.stream().anyMatch(o -> matches(o.getParams()));
    }
    public ParamMapMapper withMatchValue(String matchValue)
    {
        return (ParamMapMapper) super.withMatchValue(matchValue);
    }
    private boolean matches(ParamsMap paramMap)
    {
        if(paramMap == null) return false;
        for(Map.Entry<String, Object> paramMapEntry : paramMap.entrySet())
        {
            final String paramMapKey = paramMapEntry.getKey();
            final String paramMapValue = paramMapEntry.getValue() != null ? paramMapEntry.getValue().toString() : null;
            if(matches(paramMapKey, paramMapValue))
                return true;
        }
        return false;
    }
}
