package com.comcast.pop.cp.endpoint.resourcepool.insight.mapper;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.operation.Operation;

import java.util.List;

public class OperationNameMapper extends AbstractInsightMapper
{
    public OperationNameMapper()
    {
        super(InsightMapperRegistry.operationName.name());
    }

    @Override
    public boolean matches(Agenda agenda)
    {
        if(agenda == null) return false;
        List<Operation> operations = agenda.getOperations();
        if(operations == null || operations.size() == 0) return false;
        return operations.stream().anyMatch(o -> matches(o.getName()));
    }
    public OperationNameMapper withMatchValue(String matchValue)
    {
        return (OperationNameMapper) super.withMatchValue(matchValue);
    }
}
